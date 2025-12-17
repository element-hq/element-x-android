/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.verification

import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.verification.SessionVerificationData
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerificationServiceListener
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.api.verification.VerificationEmoji
import io.element.android.libraries.matrix.api.verification.VerificationFlowState
import io.element.android.libraries.matrix.api.verification.VerificationRequest
import io.element.android.libraries.matrix.impl.util.cancelAndDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.Encryption
import org.matrix.rustcomponents.sdk.RecoveryState
import org.matrix.rustcomponents.sdk.RecoveryStateListener
import org.matrix.rustcomponents.sdk.SessionVerificationController
import org.matrix.rustcomponents.sdk.SessionVerificationControllerDelegate
import org.matrix.rustcomponents.sdk.VerificationState
import org.matrix.rustcomponents.sdk.VerificationStateListener
import org.matrix.rustcomponents.sdk.use
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds
import org.matrix.rustcomponents.sdk.SessionVerificationData as RustSessionVerificationData
import org.matrix.rustcomponents.sdk.SessionVerificationRequestDetails as RustSessionVerificationRequestDetails

class RustSessionVerificationService(
    private val client: Client,
    isSyncServiceReady: Flow<Boolean>,
    private val sessionCoroutineScope: CoroutineScope,
) : SessionVerificationService, SessionVerificationControllerDelegate {
    private var currentVerificationRequest: VerificationRequest? = null

    private val encryptionService: Encryption = client.encryption()
    private lateinit var verificationController: SessionVerificationController

    private val _verificationFlowState = MutableStateFlow<VerificationFlowState>(VerificationFlowState.Initial)
    override val verificationFlowState = _verificationFlowState.asStateFlow()

    private val _sessionVerifiedStatus = MutableStateFlow<SessionVerifiedStatus>(SessionVerifiedStatus.Unknown)
    override val sessionVerifiedStatus: StateFlow<SessionVerifiedStatus> = _sessionVerifiedStatus.asStateFlow()

    private val recoveryState = MutableStateFlow(RecoveryState.UNKNOWN)

    // Listen for changes in verification status and update accordingly
    private val verificationStateListenerTaskHandle = encryptionService.verificationStateListener(object : VerificationStateListener {
        override fun onUpdate(status: VerificationState) {
            Timber.d("New verification state: $status")
            _sessionVerifiedStatus.value = status.map()
        }
    })

    // In case we enter the recovery key instead we check changes in the recovery state, since the listener above won't be triggered
    private val recoveryStateListenerTaskHandle = encryptionService.recoveryStateListener(object : RecoveryStateListener {
        override fun onUpdate(status: RecoveryState) {
            Timber.d("New recovery state: $status")
            // We could check the `RecoveryState`, but it's easier to just use the verification state directly
            recoveryState.value = status
        }
    })

    /**
     * The internal service that checks verification can only run after the initial sync.
     * This [StateFlow] will notify consumers when the service is ready to be used.
     */
    private val isReady = isSyncServiceReady.stateIn(sessionCoroutineScope, SharingStarted.Eagerly, false)

    override val needsSessionVerification = sessionVerifiedStatus.map { verificationStatus ->
        verificationStatus == SessionVerifiedStatus.NotVerified
    }

    override fun didReceiveVerificationRequest(details: RustSessionVerificationRequestDetails) {
        listener?.onIncomingSessionRequest(details.toVerificationRequest(UserId(client.userId())))
    }

    private var listener: SessionVerificationServiceListener? = null

    init {
        // Instantiate the verification controller when possible, this is needed to get incoming verification requests
        sessionCoroutineScope.launch {
            tryOrNull {
                encryptionService.waitForE2eeInitializationTasks()
                initVerificationControllerIfNeeded()
            }
        }
    }

    override fun setListener(listener: SessionVerificationServiceListener?) {
        this.listener = listener
    }

    override suspend fun requestCurrentSessionVerification() = tryOrFail {
        initVerificationControllerIfNeeded()
        verificationController.requestDeviceVerification()
        currentVerificationRequest = VerificationRequest.Outgoing.CurrentSession
    }

    override suspend fun requestUserVerification(userId: UserId) = tryOrFail {
        initVerificationControllerIfNeeded()
        verificationController.requestUserVerification(userId.value)
        currentVerificationRequest = VerificationRequest.Outgoing.User(userId)
    }

    override suspend fun cancelVerification() = tryOrFail {
        verificationController.cancelVerification()
        // We need to manually set the state to canceled, as the Rust SDK doesn't always call `didCancel` when it should
        didCancel()
    }

    override suspend fun approveVerification() = tryOrFail { verificationController.approveVerification() }

    override suspend fun declineVerification() = tryOrFail { verificationController.declineVerification() }

    override suspend fun startVerification() = tryOrFail {
        verificationController.startSasVerification()
    }

    override suspend fun acknowledgeVerificationRequest(verificationRequest: VerificationRequest.Incoming) = tryOrFail {
        initVerificationControllerIfNeeded()
        verificationController.acknowledgeVerificationRequest(
            senderId = verificationRequest.details.senderProfile.userId.value,
            flowId = verificationRequest.details.flowId.value,
        )
    }

    override suspend fun acceptVerificationRequest() = tryOrFail {
        Timber.d("Accepting incoming verification request")
        verificationController.acceptVerificationRequest()
    }

    private suspend fun tryOrFail(block: suspend () -> Unit) {
        runCatchingExceptions {
            // Ensure the block cannot be cancelled, else if the Rust SDK emit a new state during the API execution,
            // the state machine may cancel the api call.
            withContext(NonCancellable) {
                block()
            }
        }.onFailure {
            Timber.e(it, "Failed to verify session")
            didFail()
        }
    }

    // region Delegate implementation

    // When verification attempt is accepted by the other device
    override fun didAcceptVerificationRequest() {
        _verificationFlowState.value = VerificationFlowState.DidAcceptVerificationRequest
    }

    override fun didCancel() {
        _verificationFlowState.value = VerificationFlowState.DidCancel
    }

    override fun didFail() {
        Timber.e("Session verification failed with an unknown error")
        _verificationFlowState.value = VerificationFlowState.DidFail
    }

    override fun didFinish() {
        sessionCoroutineScope.launch {
            // Ideally this should be `verificationController?.isVerified().orFalse()` but for some reason it returns false if run immediately
            // It also sometimes unexpectedly fails to report the session as verified, so we have to handle that possibility and fail if needed
            runCatchingExceptions {
                withTimeout(20.seconds) {
                    // Wait until the SDK reports the state as verified
                    sessionVerifiedStatus.first { it == SessionVerifiedStatus.Verified }
                }
            }
                .onSuccess {
                    if (currentVerificationRequest is VerificationRequest.Outgoing.CurrentSession) {
                        // Try waiting for the final recovery state for better UX, but don't block the verification state on it
                        tryOrNull {
                            withTimeout(10.seconds) {
                                // Wait until the recovery state is either fully loaded or we check it's explicitly disabled
                                recoveryState.first { it == RecoveryState.ENABLED || it == RecoveryState.DISABLED }
                            }
                        }
                    }

                    _verificationFlowState.value = VerificationFlowState.DidFinish
                    updateVerificationStatus()
                }
                .onFailure {
                    Timber.e(it, "Verification finished, but the Rust SDK still reports the session as unverified.")
                    didFail()
                }
        }
    }

    override fun didReceiveVerificationData(data: RustSessionVerificationData) {
        _verificationFlowState.value = VerificationFlowState.DidReceiveVerificationData(data.map())
    }

    // When the actual SAS verification starts
    override fun didStartSasVerification() {
        _verificationFlowState.value = VerificationFlowState.DidStartSasVerification
    }

    // end-region

    override suspend fun reset(cancelAnyPendingVerificationAttempt: Boolean) {
        currentVerificationRequest = null
        if (isReady.value && cancelAnyPendingVerificationAttempt) {
            // Cancel any pending verification attempt
            tryOrNull { verificationController.cancelVerification() }
        }
        _verificationFlowState.value = VerificationFlowState.Initial
    }

    fun destroy() {
        Timber.d("Destroying RustSessionVerificationService")
        verificationStateListenerTaskHandle.cancelAndDestroy()
        recoveryStateListenerTaskHandle.cancelAndDestroy()
        if (this::verificationController.isInitialized) {
            verificationController.setDelegate(null)
        }
    }

    private var initControllerMutex = Mutex()

    private suspend fun initVerificationControllerIfNeeded() = initControllerMutex.withLock {
        if (!this::verificationController.isInitialized) {
            tryOrFail {
                verificationController = client.getSessionVerificationController()
                verificationController.setDelegate(this)
            }
        }
    }

    private fun updateVerificationStatus() {
        runCatchingExceptions {
            _sessionVerifiedStatus.value = encryptionService.verificationState().map()
            Timber.d("New verification status: ${_sessionVerifiedStatus.value}")
        }
    }
}

private fun VerificationState.map() = when (this) {
    VerificationState.UNKNOWN -> SessionVerifiedStatus.Unknown
    VerificationState.VERIFIED -> SessionVerifiedStatus.Verified
    VerificationState.UNVERIFIED -> SessionVerifiedStatus.NotVerified
}

private fun RustSessionVerificationData.map(): SessionVerificationData {
    return use { sessionVerificationData ->
        when (sessionVerificationData) {
            is RustSessionVerificationData.Emojis -> {
                SessionVerificationData.Emojis(
                    emojis = sessionVerificationData.emojis.mapIndexed { index, emoji ->
                        emoji.use { sessionVerificationEmoji ->
                            VerificationEmoji(
                                number = sessionVerificationData.indices[index].toInt(),
                            )
                        }
                    },
                )
            }
            is RustSessionVerificationData.Decimals -> {
                SessionVerificationData.Decimals(
                    decimals = sessionVerificationData.values.map { it.toInt() },
                )
            }
        }
    }
}
