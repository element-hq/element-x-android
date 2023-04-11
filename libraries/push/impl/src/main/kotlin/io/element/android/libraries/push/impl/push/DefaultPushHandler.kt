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

package io.element.android.libraries.push.impl.push

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.androidutils.network.WifiDetector
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.push.api.store.PushDataStore
import io.element.android.libraries.push.impl.PushersManager
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import io.element.android.libraries.push.impl.log.pushLoggerTag
import io.element.android.libraries.push.impl.notifications.NotifiableEventResolver
import io.element.android.libraries.push.impl.notifications.NotificationActionIds
import io.element.android.libraries.push.impl.notifications.NotificationDrawerManager
import io.element.android.libraries.push.impl.store.DefaultPushDataStore
import io.element.android.libraries.push.providers.api.PushData
import io.element.android.libraries.push.providers.api.PushHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private val loggerTag = LoggerTag("PushHandler", pushLoggerTag)

@ContributesBinding(AppScope::class)
class DefaultPushHandler @Inject constructor(
    private val notificationDrawerManager: NotificationDrawerManager,
    private val notifiableEventResolver: NotifiableEventResolver,
    private val pushDataStore: PushDataStore,
    private val defaultPushDataStore: DefaultPushDataStore,
    private val pushClientSecret: PushClientSecret,
    private val actionIds: NotificationActionIds,
    @ApplicationContext private val context: Context,
    private val buildMeta: BuildMeta,
    private val matrixAuthenticationService: MatrixAuthenticationService,
): PushHandler {

    private val coroutineScope = CoroutineScope(SupervisorJob())
    private val wifiDetector: WifiDetector = WifiDetector(context)

    // UI handler
    private val mUIHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    /**
     * Called when message is received.
     *
     * @param pushData the data received in the push.
     */
    override suspend fun handle(pushData: PushData) {
        Timber.tag(loggerTag.value).d("## handling pushData")

        if (buildMeta.lowPrivacyLoggingEnabled) {
            Timber.tag(loggerTag.value).d("## pushData: $pushData")
        }

        defaultPushDataStore.incrementPushCounter()

        // Diagnostic Push
        if (pushData.eventId == PushersManager.TEST_EVENT_ID) {
            val intent = Intent(actionIds.push)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            return
        }

        // TODO EAx Should be per user
        if (!pushDataStore.areNotificationEnabledForDevice()) {
            Timber.tag(loggerTag.value).i("Notification are disabled for this device")
            return
        }

        mUIHandler.post {
            coroutineScope.launch(Dispatchers.IO) { handleInternal(pushData) }
        }
    }

    /**
     * Internal receive method.
     *
     * @param pushData Object containing message data.
     */
    private suspend fun handleInternal(pushData: PushData) {
        try {
            if (buildMeta.lowPrivacyLoggingEnabled) {
                Timber.tag(loggerTag.value).d("## handleInternal() : $pushData")
            } else {
                Timber.tag(loggerTag.value).d("## handleInternal()")
            }

            pushData.roomId ?: return
            pushData.eventId ?: return

            val clientSecret = pushData.clientSecret
            val userId = if (clientSecret == null) {
                // Should not happen. In this case, restore default session
                null
            } else {
                // Get userId from client secret
                pushClientSecret.getUserIdFromSecret(clientSecret)
            } ?: run {
                matrixAuthenticationService.getLatestSessionId()
            }

            if (userId == null) {
                Timber.w("Unable to get a session")
                return
            }

            val notificationData = notifiableEventResolver.resolveEvent(userId, pushData.roomId, pushData.eventId)

            if (notificationData == null) {
                Timber.w("Unable to get a notification data")
                return
            }

            notificationDrawerManager.onNotifiableEventReceived(notificationData)
        } catch (e: Exception) {
            Timber.tag(loggerTag.value).e(e, "## handleInternal() failed")
        }
    }
}
