/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.verification

import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.matrix.api.verification.SessionVerificationData
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.api.verification.VerificationEmoji
import io.element.android.libraries.matrix.api.verification.VerificationFlowState
import io.element.android.libraries.matrix.impl.util.cancelAndDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import org.matrix.rustcomponents.sdk.ClientInterface
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

class RustSessionVerificationService(
    private val client: ClientInterface,
    isSyncServiceReady: Flow<Boolean>,
    private val sessionCoroutineScope: CoroutineScope,
) : SessionVerificationService, SessionVerificationControllerDelegate {
    private val encryptionService: Encryption = client.encryption()
    private lateinit var verificationController: SessionVerificationController

    private val _verificationFlowState = MutableStateFlow<VerificationFlowState>(VerificationFlowState.Initial)
    override val verificationFlowState = _verificationFlowState.asStateFlow()

    private val _sessionVerifiedStatus = MutableStateFlow<SessionVerifiedStatus>(SessionVerifiedStatus.Unknown)
    override val sessionVerifiedStatus: StateFlow<SessionVerifiedStatus> = _sessionVerifiedStatus.asStateFlow()

    // Listen for changes in verification status and update accordingly
    private val verificationStateListenerTaskHandle = encryptionService.verificationStateListener(object : VerificationStateListener {
        override fun onUpdate(status: VerificationState) {
            Timber.d("New verification state: $status")
            sessionCoroutineScope.launch { updateVerificationStatus() }
        }
    })

    // In case we enter the recovery key instead we check changes in the recovery state, since the listener above won't be triggered
    private val recoveryStateListenerTaskHandle = encryptionService.recoveryStateListener(object : RecoveryStateListener {
        override fun onUpdate(status: RecoveryState) {
            Timber.d("New recovery state: $status")
            // We could check the `RecoveryState`, but it's easier to just use the verification state directly
            sessionCoroutineScope.launch { updateVerificationStatus() }
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

    init {
        // Update initial state in case sliding sync isn't ready
        sessionCoroutineScope.launch { updateVerificationStatus() }

        isReady.onEach { isReady ->
            if (isReady) {
                Timber.d("Starting verification service")
                // Immediate status update
                updateVerificationStatus()
            } else {
                Timber.d("Stopping verification service")
                updateVerificationStatus()
            }
        }
            .launchIn(sessionCoroutineScope)
    }

    override suspend fun requestVerification() = tryOrFail {
        initVerificationControllerIfNeeded()
        verificationController.requestVerification()
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

    private suspend fun tryOrFail(block: suspend () -> Unit) {
        runCatching {
            block()
        }.onFailure {
            Timber.e(it, "Failed to verify session")
            didFail()
        }
    }

    // region Delegate implementation

    // When verification attempt is accepted by the other device
    override fun didAcceptVerificationRequest() {
        _verificationFlowState.value = VerificationFlowState.AcceptedVerificationRequest
    }

    override fun didCancel() {
        _verificationFlowState.value = VerificationFlowState.Canceled
    }

    override fun didFail() {
        Timber.e("Session verification failed with an unknown error")
        _verificationFlowState.value = VerificationFlowState.Failed
    }

    override fun didFinish() {
        sessionCoroutineScope.launch {
            // Ideally this should be `verificationController?.isVerified().orFalse()` but for some reason it returns false if run immediately
            // It also sometimes unexpectedly fails to report the session as verified, so we have to handle that possibility and fail if needed
            runCatching {
                withTimeout(30.seconds) {
                    while (!verificationController.isVerified()) {
                        delay(100)
                    }
                }
            }
                .onSuccess {
                    // Order here is important, first set the flow state as finished, then update the verification status
                    _verificationFlowState.value = VerificationFlowState.Finished
                    updateVerificationStatus()
                }
                .onFailure {
                    Timber.e(it, "Verification finished, but the Rust SDK still reports the session as unverified.")
                    didFail()
                }
        }
    }

    override fun didReceiveVerificationData(data: RustSessionVerificationData) {
        _verificationFlowState.value = VerificationFlowState.ReceivedVerificationData(data.map())
    }

    // When the actual SAS verification starts
    override fun didStartSasVerification() {
        _verificationFlowState.value = VerificationFlowState.StartedSasVerification
    }

    // end-region

    override suspend fun reset() {
        if (isReady.value) {
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
            verificationController.destroy()
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

    private suspend fun updateVerificationStatus() {
        if (verificationFlowState.value == VerificationFlowState.Finished) {
            // Calling `encryptionService.verificationState()` performs a network call and it will deadlock if there is no network
            // So we need to check that *only* if we know there is network connection, which is the case when the verification flow just finished
            Timber.d("Updating verification status: flow just finished")
            runCatching {
                encryptionService.waitForE2eeInitializationTasks()
            }.onSuccess {
                _sessionVerifiedStatus.value = when (encryptionService.verificationState()) {
                    VerificationState.UNKNOWN -> SessionVerifiedStatus.Unknown
                    VerificationState.VERIFIED -> SessionVerifiedStatus.Verified
                    VerificationState.UNVERIFIED -> SessionVerifiedStatus.NotVerified
                }
                Timber.d("New verification status: ${_sessionVerifiedStatus.value}")
            }
        } else {
            // Otherwise, just check the current verification status from the session verification controller instead
            Timber.d("Updating verification status: flow is pending or was finished some time ago")
            runCatching {
                initVerificationControllerIfNeeded()
                _sessionVerifiedStatus.value = if (verificationController.isVerified()) {
                    SessionVerifiedStatus.Verified
                } else {
                    SessionVerifiedStatus.NotVerified
                }
                Timber.d("New verification status: ${_sessionVerifiedStatus.value}")
            }
        }
    }
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
                                emoji = sessionVerificationEmoji.symbol(),
                                description = sessionVerificationEmoji.description(),
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
