/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.appconfig.PushConfig
import io.element.android.libraries.core.extensions.mapFailure
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.exception.ClientException
import io.element.android.libraries.matrix.api.pusher.SetHttpPusherData
import io.element.android.libraries.matrix.api.pusher.UnsetHttpPusherData
import io.element.android.libraries.pushproviders.api.PusherSubscriber
import io.element.android.libraries.pushproviders.api.RegistrationFailure
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import timber.log.Timber

internal const val DEFAULT_PUSHER_FILE_TAG = "mobile"

private val loggerTag = LoggerTag("DefaultPusherSubscriber", LoggerTag.PushLoggerTag)

@ContributesBinding(AppScope::class)
class DefaultPusherSubscriber(
    private val buildMeta: BuildMeta,
    private val pushClientSecret: PushClientSecret,
    private val userPushStoreFactory: UserPushStoreFactory,
) : PusherSubscriber {
    /**
     * Register a pusher to the server if not done yet.
     */
    override suspend fun registerPusher(
        matrixClient: MatrixClient,
        pushKey: String,
        gateway: String,
    ): Result<Unit> {
        val userDataStore = userPushStoreFactory.getOrCreate(matrixClient.sessionId)
        val isRegisteringAgain = userDataStore.getCurrentRegisteredPushKey() == pushKey
        if (isRegisteringAgain) {
            Timber.tag(loggerTag.value)
                .d("Unnecessary to register again the same pusher, but do it in case the pusher has been removed from the server")
        }
        return matrixClient.pushersService
            .setHttpPusher(
                createHttpPusher(pushKey, gateway, matrixClient.sessionId)
            )
            .onSuccess {
                userDataStore.setCurrentRegisteredPushKey(pushKey)
            }
            .mapFailure { throwable ->
                Timber.tag(loggerTag.value).e(throwable, "Unable to register the pusher")
                if (throwable is ClientException) {
                    // It should always be the case.
                    RegistrationFailure(throwable, isRegisteringAgain = isRegisteringAgain)
                } else {
                    throwable
                }
            }
    }

    private suspend fun createHttpPusher(
        pushKey: String,
        gateway: String,
        userId: SessionId,
    ): SetHttpPusherData =
        SetHttpPusherData(
            pushKey = pushKey,
            appId = PushConfig.PUSHER_APP_ID,
            // TODO + abs(activeSessionHolder.getActiveSession().myUserId.hashCode())
            profileTag = DEFAULT_PUSHER_FILE_TAG + "_",
            // TODO localeProvider.current().language
            lang = "en",
            appDisplayName = buildMeta.applicationName,
            // TODO getDeviceInfoUseCase.execute().displayName().orEmpty()
            deviceDisplayName = "MyDevice",
            url = gateway,
            defaultPayload = createDefaultPayload(pushClientSecret.getSecretForUser(userId))
        )

    /**
     * Ex: {"cs":"sfvsdv"}.
     */
    private fun createDefaultPayload(secretForUser: String): String {
        return "{\"cs\":\"$secretForUser\"}"
    }

    override suspend fun unregisterPusher(
        matrixClient: MatrixClient,
        pushKey: String,
        gateway: String,
    ): Result<Unit> {
        val userDataStore = userPushStoreFactory.getOrCreate(matrixClient.sessionId)
        return matrixClient.pushersService
            .unsetHttpPusher(
                unsetHttpPusherData = UnsetHttpPusherData(
                    pushKey = pushKey,
                    appId = PushConfig.PUSHER_APP_ID
                )
            )
            .onSuccess {
                userDataStore.setCurrentRegisteredPushKey(null)
            }
            .onFailure { throwable ->
                Timber.tag(loggerTag.value).e(throwable, "Unable to unregister the pusher")
            }
    }
}
