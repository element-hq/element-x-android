/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl.setup

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.appconfig.PrivatePushConfig
import io.element.android.features.privatepush.api.PrivatePushService
import io.element.android.features.privatepush.api.PrivatePushStatus
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.push.api.PushService
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * App-scoped owner of the "Activate notifications" sequence (twin of [io.element.android.features.privatepush.impl.install.NtfyInstaller]).
 *
 * The sequence drops a stale (public) registration, registers with the ntfy distributor and
 * verifies the endpoint host. It must never be cut in the middle: a presenter leaving composition
 * (notification tap, configuration change, system back) would otherwise leave the session with no
 * pusher at all. Running it on the application scope guarantees it completes; the presenter simply
 * observes [state].
 */
@SingleIn(AppScope::class)
@Inject
class PrivatePushConnector(
    private val pushService: PushService,
    private val privatePushService: PrivatePushService,
    @AppCoroutineScope private val appCoroutineScope: CoroutineScope,
) {
    private val _state = MutableStateFlow<ConnectState>(ConnectState.Idle)
    val state: StateFlow<ConnectState> = _state.asStateFlow()

    private var job: Job? = null

    val isRunning: Boolean
        get() = job?.isActive == true

    /** Starts the sequence unless one is already running. */
    fun connect(matrixClient: MatrixClient, provider: PushProvider, distributor: Distributor) {
        if (isRunning) return
        _state.value = ConnectState.Connecting
        job = appCoroutineScope.launch {
            val sessionId = matrixClient.sessionId
            val status = privatePushService.status(sessionId)
            val registration = if (status is PrivatePushStatus.PublicServer) {
                reRegister(matrixClient, provider, distributor)
            } else {
                register(matrixClient, provider, distributor)
            }
            val verdict = registration.getOrElse { error ->
                Timber.w(error, "Private push registration failed")
                _state.value = ConnectState.Problem(ConnectProblem.RegistrationFailed(error.message))
                return@launch
            }
            // ntfy may answer a REGISTER for a still-known token with the previous endpoint when
            // the UNREGISTER has not been processed yet: give it one more chance before blaming
            // the member's settings.
            val finalVerdict = if (verdict is PrivatePushStatus.PublicServer) {
                reRegister(matrixClient, provider, distributor).getOrElse { error ->
                    Timber.w(error, "Private push re-registration failed")
                    _state.value = ConnectState.Problem(ConnectProblem.RegistrationFailed(error.message))
                    return@launch
                }
            } else {
                verdict
            }
            _state.value = when (finalVerdict) {
                PrivatePushStatus.Private -> {
                    privatePushService.setDismissed(sessionId, false)
                    privatePushService.clearSetupRequest(sessionId)
                    ConnectState.Connected
                }
                is PrivatePushStatus.PublicServer -> ConnectState.Problem(ConnectProblem.WrongServer(finalVerdict.host))
                is PrivatePushStatus.NotSetUp -> when (finalVerdict.reason) {
                    PrivatePushStatus.NotSetUp.Reason.NtfyNotInstalled -> ConnectState.Problem(ConnectProblem.NtfyNotInstalled)
                    PrivatePushStatus.NotSetUp.Reason.NotConnected -> ConnectState.Problem(ConnectProblem.RegistrationFailed(null))
                }
            }
        }
    }

    /** Forgets the last verdict. No-op while a sequence is running (it is never cancelled). */
    fun reset() {
        if (isRunning) return
        _state.value = ConnectState.Idle
    }

    private suspend fun register(matrixClient: MatrixClient, provider: PushProvider, distributor: Distributor): Result<PrivatePushStatus> {
        return pushService.registerWith(matrixClient, provider, distributor)
            .map { privatePushService.status(matrixClient.sessionId) }
    }

    /**
     * A stale endpoint (e.g. ntfy.sh) is re-sent by ntfy for a known registration and
     * registerWith() skips the unregister when provider+distributor are unchanged: drop it
     * first so a fresh endpoint is issued on the server ntfy is now pointed to.
     */
    private suspend fun reRegister(matrixClient: MatrixClient, provider: PushProvider, distributor: Distributor): Result<PrivatePushStatus> {
        provider.unregister(matrixClient).onFailure { error ->
            Timber.w(error, "Private push: could not drop the previous registration")
        }
        delay(PrivatePushConfig.REREGISTER_DELAY_MS)
        return register(matrixClient, provider, distributor)
    }
}
