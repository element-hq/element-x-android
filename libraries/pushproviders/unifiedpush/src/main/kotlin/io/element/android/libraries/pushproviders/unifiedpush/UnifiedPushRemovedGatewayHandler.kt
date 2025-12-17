/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.androidutils.throttler.FirstThrottler
import io.element.android.libraries.core.extensions.flatMap
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.push.api.PushService
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber

private val loggerTag = LoggerTag("UnifiedPushRemovedGatewayHandler", LoggerTag.PushLoggerTag)

/**
 * Handle endpoint removal received from UnifiedPush. Will try to register again.
 */
fun interface UnifiedPushRemovedGatewayHandler {
    suspend fun handle(clientSecret: String): Result<Unit>
}

@Inject
@SingleIn(AppScope::class)
class UnifiedPushRemovedGatewayThrottler(
    @AppCoroutineScope
    private val appCoroutineScope: CoroutineScope,
) {
    private val firstThrottler = FirstThrottler(
        minimumInterval = 60_000,
        coroutineScope = appCoroutineScope,
    )

    fun canRegisterAgain(): Boolean {
        return firstThrottler.canHandle()
    }
}

@ContributesBinding(AppScope::class)
class DefaultUnifiedPushRemovedGatewayHandler(
    private val unregisterUnifiedPushUseCase: UnregisterUnifiedPushUseCase,
    private val pushClientSecret: PushClientSecret,
    private val matrixClientProvider: MatrixClientProvider,
    private val pushService: PushService,
    private val unifiedPushRemovedGatewayThrottler: UnifiedPushRemovedGatewayThrottler,
) : UnifiedPushRemovedGatewayHandler {
    /**
     * The application has been informed by the UnifiedPush distributor that the topic has been deleted.
     * So this code aim to unregister the pusher from the homeserver, register a new topic on the
     * UnifiedPush application then register a new pusher to the homeserver.
     * No registration will happen if the topic deletion has already occurred in the last minute.
     */
    override suspend fun handle(clientSecret: String): Result<Unit> {
        val sessionId = pushClientSecret.getUserIdFromSecret(clientSecret) ?: return Result.failure<Unit>(
            IllegalStateException("Unable to retrieve session")
        ).also {
            Timber.tag(loggerTag.value).w("Unable to retrieve session")
        }
        return matrixClientProvider
            .getOrRestore(sessionId)
            .onFailure {
                // Silently ignore this error (do not invoke onServiceUnregistered)
                Timber.tag(loggerTag.value).w(it, "Fails to restore client")
            }
            .flatMap { client ->
                client.rotateRegistration(clientSecret = clientSecret)
                    .onFailure {
                        Timber.tag(loggerTag.value).w(it, "Issue during pusher unregistration / re registration")
                        // Let the user know
                        pushService.onServiceUnregistered(sessionId)
                    }
            }
    }

    /**
     * Unregister the pusher for the session. Then register again if possible.
     */
    private suspend fun MatrixClient.rotateRegistration(clientSecret: String): Result<Unit> {
        val unregisterResult = unregisterUnifiedPushUseCase.unregister(
            matrixClient = this,
            clientSecret = clientSecret,
            unregisterUnifiedPush = false,
        ).onFailure {
            Timber.tag(loggerTag.value).w(it, "Unable to unregister pusher")
        }
        return unregisterResult.flatMap {
            registerAgain()
        }
    }

    /**
     * Attempt to register again, if possible i.e. the current configuration is known and the
     * deletion of data in the UnifiedPush application has not already occurred in the last minute.
     */
    private suspend fun MatrixClient.registerAgain(): Result<Unit> {
        return if (unifiedPushRemovedGatewayThrottler.canRegisterAgain()) {
            val pushProvider = pushService.getCurrentPushProvider(sessionId)
            val distributor = pushProvider?.getCurrentDistributor(sessionId)
            if (pushProvider != null && distributor != null) {
                pushService.registerWith(
                    matrixClient = this,
                    pushProvider = pushProvider,
                    distributor = distributor,
                ).onFailure {
                    Timber.tag(loggerTag.value).w(it, "Unable to register with current data")
                }
            } else {
                Result.failure(IllegalStateException("Unable to register again"))
            }
        } else {
            Timber.tag(loggerTag.value).w("Second removal in less than 1 minute, do not register again")
            Result.failure(IllegalStateException("Too many requests to register again"))
        }
    }
}
