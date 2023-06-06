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
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.pusher.SetHttpPusherData
import io.element.android.libraries.push.impl.config.PushConfig
import io.element.android.libraries.push.impl.log.pushLoggerTag
import io.element.android.libraries.push.impl.pushgateway.PushGatewayNotifyRequest
import io.element.android.libraries.pushproviders.api.PusherSubscriber
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import io.element.android.services.toolbox.api.appname.AppNameProvider
import timber.log.Timber
import javax.inject.Inject

internal const val DEFAULT_PUSHER_FILE_TAG = "mobile"

private val loggerTag = LoggerTag("PushersManager", pushLoggerTag)

@ContributesBinding(AppScope::class)
class PushersManager @Inject constructor(
    // private val localeProvider: LocaleProvider,
    private val appNameProvider: AppNameProvider,
    // private val getDeviceInfoUseCase: GetDeviceInfoUseCase,
    private val pushGatewayNotifyRequest: PushGatewayNotifyRequest,
    private val pushClientSecret: PushClientSecret,
    private val userPushStoreFactory: UserPushStoreFactory,
) : PusherSubscriber {
    // TODO Move this to the PushProvider API
    suspend fun testPush() {
        pushGatewayNotifyRequest.execute(
            PushGatewayNotifyRequest.Params(
                url = "TODO", // unifiedPushHelper.getPushGateway() ?: return,
                appId = PushConfig.pusher_app_id,
                pushKey = "TODO", // unifiedPushHelper.getEndpointOrToken().orEmpty(),
                eventId = TEST_EVENT_ID
            )
        )
    }

    /**
     * Register a pusher to the server if not done yet.
     */
    override suspend fun registerPusher(matrixClient: MatrixClient, pushKey: String, gateway: String) {
        val userDataStore = userPushStoreFactory.create(matrixClient.sessionId)
        if (userDataStore.getCurrentRegisteredPushKey() == pushKey) {
            Timber.tag(loggerTag.value).d("Unnecessary to register again the same pusher")
        } else {
            // Register the pusher to the server
            matrixClient.pushersService().setHttpPusher(
                createHttpPusher(pushKey, gateway, matrixClient.sessionId)
            ).fold(
                {
                    userDataStore.setCurrentRegisteredPushKey(pushKey)
                },
                { throwable ->
                    Timber.tag(loggerTag.value).e(throwable, "Unable to register the pusher")
                }
            )
        }
    }

    private suspend fun createHttpPusher(
        pushKey: String,
        gateway: String,
        userId: SessionId,
    ): SetHttpPusherData =
        SetHttpPusherData(
            pushKey = pushKey,
            appId = PushConfig.pusher_app_id,
            profileTag = DEFAULT_PUSHER_FILE_TAG + "_" /* TODO + abs(activeSessionHolder.getActiveSession().myUserId.hashCode())*/,
            lang = "en", // TODO localeProvider.current().language,
            appDisplayName = appNameProvider.getAppName(),
            deviceDisplayName = "MyDevice", // TODO getDeviceInfoUseCase.execute().displayName().orEmpty(),
            url = gateway,
            defaultPayload = createDefaultPayload(pushClientSecret.getSecretForUser(userId))
        )

    /**
     * Ex: {"cs":"sfvsdv"}.
     */
    private fun createDefaultPayload(secretForUser: String): String {
        return "{\"cs\":\"$secretForUser\"}"
    }

    suspend fun registerEmailForPush(email: String) {
        TODO()
        /*
        val currentSession = activeSessionHolder.getActiveSession()
        val appName = appNameProvider.getAppName()
        currentSession.pushersService().addEmailPusher(
            email = email,
            lang = localeProvider.current().language,
            emailBranding = appName,
            appDisplayName = appName,
            deviceDisplayName = currentSession.sessionParams.deviceId ?: "MOBILE"
        )
        */
    }

    fun getPusherForCurrentSession() {}/*: Pusher? {
        val session = activeSessionHolder.getSafeActiveSession() ?: return null
        val deviceId = session.sessionParams.deviceId
        return session.pushersService().getPushers().firstOrNull { it.deviceId == deviceId }
    }
    */

    suspend fun unregisterEmailPusher(email: String) {
        // val currentSession = activeSessionHolder.getSafeActiveSession() ?: return
        // currentSession.pushersService().removeEmailPusher(email)
    }

    override suspend fun unregisterPusher(matrixClient: MatrixClient, pushKey: String, gateway: String) {
        matrixClient.pushersService().unsetHttpPusher()
    }

    companion object {
        val TEST_EVENT_ID = EventId("\$THIS_IS_A_FAKE_EVENT_ID")
    }
}
