/*
 * Copyright 2019 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.push.impl

import io.element.android.libraries.push.impl.config.PushConfig
import io.element.android.libraries.push.impl.pushgateway.PushGatewayNotifyRequest
import io.element.android.libraries.toolbox.api.appname.AppNameProvider
import java.util.UUID
import javax.inject.Inject

internal const val DEFAULT_PUSHER_FILE_TAG = "mobile"

// TODO EAx Communicate with the SDK
class PushersManager @Inject constructor(
    private val unifiedPushHelper: UnifiedPushHelper,
    // private val activeSessionHolder: ActiveSessionHolder,
    // private val localeProvider: LocaleProvider,
    private val appNameProvider: AppNameProvider,
    // private val getDeviceInfoUseCase: GetDeviceInfoUseCase,
    private val pushGatewayNotifyRequest: PushGatewayNotifyRequest,
) {
    suspend fun testPush() {
        pushGatewayNotifyRequest.execute(
            PushGatewayNotifyRequest.Params(
                url = unifiedPushHelper.getPushGateway() ?: return,
                appId = PushConfig.pusher_app_id,
                pushKey = unifiedPushHelper.getEndpointOrToken().orEmpty(),
                eventId = TEST_EVENT_ID
            )
        )
    }

    fun enqueueRegisterPusherWithFcmKey(pushKey: String)/*: UUID*/ {
        return enqueueRegisterPusher(pushKey, PushConfig.pusher_http_url)
    }

    fun enqueueRegisterPusher(
        pushKey: String,
        gateway: String
    ) /*: UUID*/ {
        /*
        val currentSession = activeSessionHolder.getActiveSession()
        val pusher = createHttpPusher(pushKey, gateway)
        return currentSession.pushersService().enqueueAddHttpPusher(pusher)

         */
        // TODO EAx
        // TODO()
        // Get all sessions
        // Register pusher
        // Close sessions
    }

    private fun createHttpPusher(
        pushKey: String,
        gateway: String
    ): Any = TODO()
    /*
    HttpPusher(
        pushkey = pushKey,
        appId = PushConfig.pusher_app_id,
        profileTag = DEFAULT_PUSHER_FILE_TAG + "_" + abs(activeSessionHolder.getActiveSession().myUserId.hashCode()),
        lang = localeProvider.current().language,
        appDisplayName = appNameProvider.getAppName(),
        deviceDisplayName = getDeviceInfoUseCase.execute().displayName().orEmpty(),
        url = gateway,
        enabled = true,
        deviceId = activeSessionHolder.getActiveSession().sessionParams.deviceId ?: "MOBILE",
        append = false,
        withEventIdOnly = true,
    )

     */

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

    suspend fun unregisterPusher(pushKey: String) {
        // val currentSession = activeSessionHolder.getSafeActiveSession() ?: return
        // currentSession.pushersService().removeHttpPusher(pushKey, PushConfig.pusher_app_id)
    }

    companion object {
        const val TEST_EVENT_ID = "\$THIS_IS_A_FAKE_EVENT_ID"
    }
}
