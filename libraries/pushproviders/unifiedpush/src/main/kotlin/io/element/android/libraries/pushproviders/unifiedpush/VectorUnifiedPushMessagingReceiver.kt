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

package io.element.android.libraries.pushproviders.unifiedpush

import android.content.Context
import android.content.Intent
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.pushproviders.api.PushHandler
import io.element.android.libraries.pushproviders.unifiedpush.registration.EndpointRegistrationHandler
import io.element.android.libraries.pushproviders.unifiedpush.registration.RegistrationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.unifiedpush.android.connector.MessagingReceiver
import timber.log.Timber
import javax.inject.Inject

private val loggerTag = LoggerTag("VectorUnifiedPushMessagingReceiver", LoggerTag.PushLoggerTag)

class VectorUnifiedPushMessagingReceiver : MessagingReceiver() {
    @Inject lateinit var pushParser: UnifiedPushParser
    @Inject lateinit var pushHandler: PushHandler
    @Inject lateinit var guardServiceStarter: GuardServiceStarter
    @Inject lateinit var unifiedPushStore: UnifiedPushStore
    @Inject lateinit var unifiedPushGatewayResolver: UnifiedPushGatewayResolver
    @Inject lateinit var newGatewayHandler: UnifiedPushNewGatewayHandler
    @Inject lateinit var endpointRegistrationHandler: EndpointRegistrationHandler
    @Inject lateinit var coroutineScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        context.applicationContext.bindings<VectorUnifiedPushMessagingReceiverBindings>().inject(this)
        super.onReceive(context, intent)
    }

    /**
     * Called when message is received. The message contains the full POST body of the push message.
     *
     * @param context the Android context
     * @param message the message
     * @param instance connection, for multi-account
     */
    override fun onMessage(context: Context, message: ByteArray, instance: String) {
        Timber.tag(loggerTag.value).d("New message")
        coroutineScope.launch {
            val pushData = pushParser.parse(message, instance)
            if (pushData == null) {
                Timber.tag(loggerTag.value).w("Invalid data received from UnifiedPush")
            } else {
                pushHandler.handle(pushData)
            }
        }
    }

    /**
     * Called when a new endpoint is to be used for sending push messages.
     * You should send the endpoint to your application server and sync for missing notifications.
     */
    override fun onNewEndpoint(context: Context, endpoint: String, instance: String) {
        Timber.tag(loggerTag.value).i("onNewEndpoint: $endpoint")
        coroutineScope.launch {
            val gateway = unifiedPushGatewayResolver.getGateway(endpoint)
            unifiedPushStore.storePushGateway(gateway, instance)
            val result = newGatewayHandler.handle(endpoint, gateway, instance)
                .onFailure {
                    Timber.tag(loggerTag.value).e(it, "Failed to handle new gateway")
                }
                .onSuccess {
                    unifiedPushStore.storeUpEndpoint(endpoint, instance)
                }
            endpointRegistrationHandler.registrationDone(
                RegistrationResult(
                    clientSecret = instance,
                    result = result,
                )
            )
        }
        guardServiceStarter.stop()
    }

    /**
     * Called when the registration is not possible, eg. no network.
     */
    override fun onRegistrationFailed(context: Context, instance: String) {
        Timber.tag(loggerTag.value).e("onRegistrationFailed for $instance")
        /*
        Toast.makeText(context, "Push service registration failed", Toast.LENGTH_SHORT).show()
        val mode = BackgroundSyncMode.FDROID_BACKGROUND_SYNC_MODE_FOR_REALTIME
        pushDataStore.setFdroidSyncBackgroundMode(mode)
        guardServiceStarter.start()
         */
    }

    /**
     * Called when this application is unregistered from receiving push messages.
     */
    override fun onUnregistered(context: Context, instance: String) {
        Timber.tag(loggerTag.value).d("Unifiedpush: Unregistered")
        /*
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
         */
    }
}
