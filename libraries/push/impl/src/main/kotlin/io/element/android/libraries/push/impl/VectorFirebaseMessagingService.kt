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

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.push.api.store.PushDataStore
import io.element.android.libraries.push.impl.config.PushConfig
import io.element.android.libraries.push.impl.di.FirebaseMessagingServiceBindings
import io.element.android.libraries.push.impl.parser.PushParser
import timber.log.Timber
import javax.inject.Inject

private val loggerTag = LoggerTag("Push", LoggerTag.SYNC)

class VectorFirebaseMessagingService : FirebaseMessagingService() {
    @Inject lateinit var fcmHelper: FcmHelper
    @Inject lateinit var pushDataStore: PushDataStore
    // @Inject lateinit var activeSessionHolder: ActiveSessionHolder
    @Inject lateinit var pushersManager: PushersManager
    @Inject lateinit var pushParser: PushParser
    @Inject lateinit var vectorPushHandler: VectorPushHandler
    @Inject lateinit var unifiedPushHelper: UnifiedPushHelper

    override fun onCreate() {
        super.onCreate()
        applicationContext.bindings<FirebaseMessagingServiceBindings>().inject(this)
    }

    override fun onNewToken(token: String) {
        Timber.tag(loggerTag.value).d("New Firebase token")
        fcmHelper.storeFcmToken(token)
        if (
            // pushDataStore.areNotificationEnabledForDevice() &&
            // TODO EAx activeSessionHolder.hasActiveSession() &&
            unifiedPushHelper.isEmbeddedDistributor()
        ) {
            pushersManager.enqueueRegisterPusher(token, PushConfig.pusher_http_url)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Timber.tag(loggerTag.value).d("New Firebase message")
        pushParser.parsePushDataFcm(message.data).let {
            vectorPushHandler.handle(it)
        }
    }
}
