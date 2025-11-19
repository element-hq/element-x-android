/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.extensions.flatMap
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.pushproviders.api.PusherSubscriber
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import timber.log.Timber

private val loggerTag = LoggerTag("DefaultUnifiedPushNewGatewayHandler", LoggerTag.PushLoggerTag)

/**
 * Handle new endpoint received from UnifiedPush. Will update the session matching the client secret.
 */
interface UnifiedPushNewGatewayHandler {
    suspend fun handle(endpoint: String, pushGateway: String, clientSecret: String): Result<Unit>
}

@ContributesBinding(AppScope::class)
class DefaultUnifiedPushNewGatewayHandler(
    private val pusherSubscriber: PusherSubscriber,
    private val userPushStoreFactory: UserPushStoreFactory,
    private val pushClientSecret: PushClientSecret,
    private val matrixClientProvider: MatrixClientProvider,
) : UnifiedPushNewGatewayHandler {
    override suspend fun handle(endpoint: String, pushGateway: String, clientSecret: String): Result<Unit> {
        // Register the pusher for the session with this client secret, if is it using UnifiedPush.
        val userId = pushClientSecret.getUserIdFromSecret(clientSecret) ?: return Result.failure<Unit>(
            IllegalStateException("Unable to retrieve session")
        ).also {
            Timber.tag(loggerTag.value).w("Unable to retrieve session")
        }
        val userDataStore = userPushStoreFactory.getOrCreate(userId)
        return if (userDataStore.getPushProviderName() == UnifiedPushConfig.NAME) {
            matrixClientProvider
                .getOrRestore(userId)
                .flatMap { client ->
                    pusherSubscriber.registerPusher(client, endpoint, pushGateway)
                }
                .onFailure {
                    Timber.tag(loggerTag.value).w(it, "Unable to register pusher")
                }
        } else {
            Timber.tag(loggerTag.value).d("This session is not using UnifiedPush pusher")
            Result.failure(
                IllegalStateException("This session is not using UnifiedPush pusher")
            )
        }
    }
}
