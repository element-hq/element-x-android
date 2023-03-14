/*
 * Copyright (c) 2022 New Vector Ltd
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

import android.content.Context
import android.content.Intent
import android.widget.Toast
import io.element.android.libraries.architecture.bindings

import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.push.api.model.BackgroundSyncMode
import io.element.android.libraries.push.api.store.PushDataStore
import io.element.android.libraries.push.impl.di.VectorUnifiedPushMessagingReceiverBindings
import io.element.android.libraries.push.impl.parser.PushParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.unifiedpush.android.connector.MessagingReceiver
import timber.log.Timber
import javax.inject.Inject

private val loggerTag = LoggerTag("Push", LoggerTag.SYNC)

class VectorUnifiedPushMessagingReceiver : MessagingReceiver() {
    @Inject lateinit var pushersManager: PushersManager
    @Inject lateinit var pushParser: PushParser

    //@Inject lateinit var activeSessionHolder: ActiveSessionHolder
    @Inject lateinit var pushDataStore: PushDataStore
    @Inject lateinit var vectorPushHandler: VectorPushHandler
    @Inject lateinit var guardServiceStarter: GuardServiceStarter
    @Inject lateinit var unifiedPushStore: UnifiedPushStore
    @Inject lateinit var unifiedPushHelper: UnifiedPushHelper

    private val coroutineScope = CoroutineScope(SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // Inject
        context.applicationContext.bindings<VectorUnifiedPushMessagingReceiverBindings>().inject(this)
    }

    /**
     * Called when message is received.
     *
     * @param context the Android context
     * @param message the message
     * @param instance connection, for multi-account
     */
    override fun onMessage(context: Context, message: ByteArray, instance: String) {
        Timber.tag(loggerTag.value).d("New message")
        pushParser.parsePushDataUnifiedPush(message)?.let {
            vectorPushHandler.handle(it)
        } ?: run {
            Timber.tag(loggerTag.value).w("Invalid received data Json format")
        }
    }

    override fun onNewEndpoint(context: Context, endpoint: String, instance: String) {
        Timber.tag(loggerTag.value).i("onNewEndpoint: adding $endpoint")
        if (pushDataStore.areNotificationEnabledForDevice() /* TODO EAx && activeSessionHolder.hasActiveSession() */) {
            // If the endpoint has changed
            // or the gateway has changed
            if (unifiedPushHelper.getEndpointOrToken() != endpoint) {
                unifiedPushStore.storeUpEndpoint(endpoint)
                coroutineScope.launch {
                    unifiedPushHelper.storeCustomOrDefaultGateway(endpoint) {
                        unifiedPushHelper.getPushGateway()?.let {
                            pushersManager.enqueueRegisterPusher(endpoint, it)
                        }
                    }
                }
            } else {
                Timber.tag(loggerTag.value).i("onNewEndpoint: skipped")
            }
        }
        val mode = BackgroundSyncMode.FDROID_BACKGROUND_SYNC_MODE_DISABLED
        pushDataStore.setFdroidSyncBackgroundMode(mode)
        guardServiceStarter.stop()
    }

    override fun onRegistrationFailed(context: Context, instance: String) {
        Toast.makeText(context, "Push service registration failed", Toast.LENGTH_SHORT).show()
        val mode = BackgroundSyncMode.FDROID_BACKGROUND_SYNC_MODE_FOR_REALTIME
        pushDataStore.setFdroidSyncBackgroundMode(mode)
        guardServiceStarter.start()
    }

    override fun onUnregistered(context: Context, instance: String) {
        Timber.tag(loggerTag.value).d("Unifiedpush: Unregistered")
        val mode = BackgroundSyncMode.FDROID_BACKGROUND_SYNC_MODE_FOR_REALTIME
        pushDataStore.setFdroidSyncBackgroundMode(mode)
        guardServiceStarter.start()
        runBlocking {
            try {
                pushersManager.unregisterPusher(unifiedPushHelper.getEndpointOrToken().orEmpty())
            } catch (e: Exception) {
                Timber.tag(loggerTag.value).d("Probably unregistering a non existing pusher")
            }
        }
    }
}
