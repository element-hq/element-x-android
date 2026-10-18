/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.verification

import dev.zacsweers.metro.Inject
import io.element.android.appnav.session.MatrixSessionCache
import io.element.android.libraries.core.coroutine.withPreviousValue
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.verification.SessionVerificationServiceListener
import io.element.android.libraries.matrix.api.verification.VerificationRequest
import io.element.android.services.appnavstate.api.AppForegroundStateService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import kotlin.time.Duration.Companion.minutes

data class IncomingVerificationRequestData(
    val sessionId: SessionId,
    val verificationRequest: VerificationRequest.Incoming,
)

/**
 * Listens to the incoming verification requests of all the sessions in [MatrixSessionCache], not only the current one.
 */
@Inject
class IncomingVerificationRequestObserver(
    private val matrixSessionCache: MatrixSessionCache,
    private val appForegroundStateService: AppForegroundStateService,
) {
    /**
     * Emits the incoming verification requests once the app is in foreground.
     * Requests received while the app is in background for too long are discarded.
     */
    fun incomingVerificationRequests(): Flow<IncomingVerificationRequestData> = channelFlow {
        launch {
            matrixSessionCache.matrixClients.withPreviousValue().collect { (previousMatrixClients, matrixClients) ->
                val previousMatrixClientSet = previousMatrixClients.orEmpty().toSet()
                // Remove the listener of the clients which are not in the cache anymore
                (previousMatrixClientSet - matrixClients.toSet()).forEach { matrixClient ->
                    matrixClient.sessionVerificationService.setListener(null)
                }
                (matrixClients - previousMatrixClientSet).forEach { matrixClient ->
                    val sessionId = matrixClient.sessionId
                    matrixClient.sessionVerificationService.setListener(
                        object : SessionVerificationServiceListener {
                            override fun onIncomingSessionRequest(verificationRequest: VerificationRequest.Incoming) {
                                launch {
                                    // Wait until the app is in foreground to display the incoming verification request
                                    // TODO there should also be a timeout for > 10 minutes elapsed since the request was created,
                                    //  but the SDK doesn't expose that info yet
                                    val isInForeground = withTimeoutOrNull(2.minutes) {
                                        appForegroundStateService.isInForeground.first { it }
                                    }
                                    if (isInForeground == null) {
                                        Timber.w("Incoming verification request ${verificationRequest.details.flowId} discarded due to timeout.")
                                    } else {
                                        send(IncomingVerificationRequestData(sessionId, verificationRequest))
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
        awaitClose {
            matrixSessionCache.matrixClients.value.forEach { matrixClient ->
                matrixClient.sessionVerificationService.setListener(null)
            }
        }
    }
}
