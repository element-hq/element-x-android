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

import android.os.Handler
import android.os.Looper
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.push.impl.PushersManager
import io.element.android.libraries.push.impl.notifications.DefaultNotificationDrawerManager
import io.element.android.libraries.push.impl.notifications.NotifiableEventResolver
import io.element.android.libraries.push.impl.store.DefaultPushDataStore
import io.element.android.libraries.push.impl.troubleshoot.DiagnosticPushHandler
import io.element.android.libraries.pushproviders.api.PushData
import io.element.android.libraries.pushproviders.api.PushHandler
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private val loggerTag = LoggerTag("PushHandler", LoggerTag.PushLoggerTag)

@ContributesBinding(AppScope::class)
class DefaultPushHandler @Inject constructor(
    private val defaultNotificationDrawerManager: DefaultNotificationDrawerManager,
    private val notifiableEventResolver: NotifiableEventResolver,
    private val defaultPushDataStore: DefaultPushDataStore,
    private val userPushStoreFactory: UserPushStoreFactory,
    private val pushClientSecret: PushClientSecret,
    // private val actionIds: NotificationActionIds,
    private val buildMeta: BuildMeta,
    private val matrixAuthenticationService: MatrixAuthenticationService,
    private val diagnosticPushHandler: DiagnosticPushHandler,
) : PushHandler {
    private val coroutineScope = CoroutineScope(SupervisorJob())

    // UI handler
    private val uiHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    /**
     * Called when message is received.
     *
     * @param pushData the data received in the push.
     */
    override suspend fun handle(pushData: PushData) {
        Timber.tag(loggerTag.value).d("## handling pushData: ${pushData.roomId}/${pushData.eventId}")

        if (buildMeta.lowPrivacyLoggingEnabled) {
            Timber.tag(loggerTag.value).d("## pushData: $pushData")
        }

        defaultPushDataStore.incrementPushCounter()

        // Diagnostic Push
        if (pushData.eventId == PushersManager.TEST_EVENT_ID) {
            diagnosticPushHandler.handlePush()
            return
        }

        uiHandler.post {
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

            val clientSecret = pushData.clientSecret
            // clientSecret should not be null. If this happens, restore default session
            val userId = clientSecret
                ?.let {
                    // Get userId from client secret
                    pushClientSecret.getUserIdFromSecret(clientSecret)
                }
                ?: run {
                    matrixAuthenticationService.getLatestSessionId()
                }

            if (userId == null) {
                Timber.w("Unable to get a session")
                return
            }

            val notifiableEvent = notifiableEventResolver.resolveEvent(userId, pushData.roomId, pushData.eventId)

            if (notifiableEvent == null) {
                Timber.w("Unable to get a notification data")
                return
            }

            val userPushStore = userPushStoreFactory.create(userId)
            if (!userPushStore.getNotificationEnabledForDevice().first()) {
                // TODO We need to check if this is an incoming call
                Timber.tag(loggerTag.value).i("Notification are disabled for this device, ignore push.")
                return
            }

            defaultNotificationDrawerManager.onNotifiableEventReceived(notifiableEvent)
        } catch (e: Exception) {
            Timber.tag(loggerTag.value).e(e, "## handleInternal() failed")
        }
    }
}
