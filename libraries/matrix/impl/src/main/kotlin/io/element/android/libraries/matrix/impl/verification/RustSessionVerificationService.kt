/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

class RustSessionVerificationService(
    private val client: Client,
    isSyncServiceReady: Flow<Boolean>,
    private val sessionCoroutineScope: CoroutineScope,
) : SessionVerificationService, SessionVerificationControllerDelegate {
    private val encryptionService: Encryption = client.encryption()
    private lateinit var verificationController: SessionVerificationController

    // Listen for changes in verification status and update accordingly
    private val verificationStateListenerTaskHandle = encryptionService.verificationStateListener(object : VerificationStateListener {
        override fun onUpdate(status: VerificationState) {
            Timber.d("New verification state: $status")
            updateVerificationStatus(status)
        }
    })

    // In case we enter the recovery key instead we check changes in the recovery state, since the listener above won't be triggered
    private val recoveryStateListenerTaskHandle = encryptionService.recoveryStateListener(object : RecoveryStateListener {
        override fun onUpdate(status: RecoveryState) {
            Timber.d("New recovery state: $status")
            // We could check the `RecoveryState`, but it's easier to just use the verification state directly
            updateVerificationStatus(encryptionService.verificationState())
        }
    })

    private val _verificationFlowState = MutableStateFlow<VerificationFlowState>(VerificationFlowState.Initial)
    override val verificationFlowState = _verificationFlowState.asStateFlow()

    private val _sessionVerifiedStatus = MutableStateFlow<SessionVerifiedStatus>(SessionVerifiedStatus.Unknown)
    override val sessionVerifiedStatus: StateFlow<SessionVerifiedStatus> = _sessionVerifiedStatus.asStateFlow()

    override val isReady = isSyncServiceReady.stateIn(sessionCoroutineScope, SharingStarted.Eagerly, false)

    override val canVerifySessionFlow = sessionVerifiedStatus.map { verificationStatus ->
        verificationStatus == SessionVerifiedStatus.NotVerified
    }

    init {
        // Update initial state in case sliding sync isn't ready
        updateVerificationStatus(encryptionService.verificationState())

        isReady.onEach { isReady ->
            if (isReady) {
                Timber.d("Starting verification service")
                // Immediate status update
                updateVerificationStatus(encryptionService.verificationState())
            } else {
                Timber.d("Stopping verification service")
            }
        }
        .launchIn(sessionCoroutineScope)
    }

    override suspend fun requestVerification() = tryOrFail {
        if (!this::verificationController.isInitialized) {
            verificationController = client.getSessionVerificationController()
            verificationController.setDelegate(this)
        }
        verificationController.requestVerification()
    }

    override suspend fun cancelVerification() = tryOrFail { verificationController.cancelVerification() }

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
                    updateVerificationStatus(VerificationState.VERIFIED)
                    _verificationFlowState.value = VerificationFlowState.Finished
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

    private fun updateVerificationStatus(verificationState: VerificationState) {
        _sessionVerifiedStatus.value = when (verificationState) {
            VerificationState.UNKNOWN -> SessionVerifiedStatus.Unknown
            VerificationState.VERIFIED -> SessionVerifiedStatus.Verified
            VerificationState.UNVERIFIED -> SessionVerifiedStatus.NotVerified
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
