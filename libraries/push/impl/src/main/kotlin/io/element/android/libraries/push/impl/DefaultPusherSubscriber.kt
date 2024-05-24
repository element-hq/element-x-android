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

package io.element.android.libraries.push.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.appconfig.PushConfig
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.pusher.SetHttpPusherData
import io.element.android.libraries.matrix.api.pusher.UnsetHttpPusherData
import io.element.android.libraries.pushproviders.api.PusherSubscriber
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import timber.log.Timber
import javax.inject.Inject

internal const val DEFAULT_PUSHER_FILE_TAG = "mobile"

private val loggerTag = LoggerTag("DefaultPusherSubscriber", LoggerTag.PushLoggerTag)

@ContributesBinding(AppScope::class)
class DefaultPusherSubscriber @Inject constructor(
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
        if (userDataStore.getCurrentRegisteredPushKey() == pushKey) {
            Timber.tag(loggerTag.value)
                .d("Unnecessary to register again the same pusher, but do it in case the pusher has been removed from the server")
        }
        return matrixClient.pushersService()
            .setHttpPusher(
                createHttpPusher(pushKey, gateway, matrixClient.sessionId)
            )
            .onSuccess {
                userDataStore.setCurrentRegisteredPushKey(pushKey)
            }
            .onFailure { throwable ->
                Timber.tag(loggerTag.value).e(throwable, "Unable to register the pusher")
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
        return matrixClient.pushersService()
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
