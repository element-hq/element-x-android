/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import android.content.Context
import android.content.Intent
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.pushproviders.api.PushHandler
import io.element.android.libraries.pushproviders.unifiedpush.registration.EndpointRegistrationHandler
import io.element.android.libraries.pushproviders.unifiedpush.registration.RegistrationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.unifiedpush.android.connector.FailedReason
import org.unifiedpush.android.connector.MessagingReceiver
import org.unifiedpush.android.connector.data.PushEndpoint
import org.unifiedpush.android.connector.data.PushMessage
import timber.log.Timber

private val loggerTag = LoggerTag("VectorUnifiedPushMessagingReceiver", LoggerTag.PushLoggerTag)

class VectorUnifiedPushMessagingReceiver : MessagingReceiver() {
    @Inject lateinit var pushParser: UnifiedPushParser
    @Inject lateinit var pushHandler: PushHandler
    @Inject lateinit var guardServiceStarter: GuardServiceStarter
    @Inject lateinit var unifiedPushStore: UnifiedPushStore
    @Inject lateinit var unifiedPushGatewayResolver: UnifiedPushGatewayResolver
    @Inject lateinit var unifiedPushGatewayUrlResolver: UnifiedPushGatewayUrlResolver
    @Inject lateinit var newGatewayHandler: UnifiedPushNewGatewayHandler
    @Inject lateinit var removedGatewayHandler: UnifiedPushRemovedGatewayHandler
    @Inject lateinit var endpointRegistrationHandler: EndpointRegistrationHandler

    @AppCoroutineScope
    @Inject lateinit var coroutineScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        context.bindings<VectorUnifiedPushMessagingReceiverBindings>().inject(this)
        super.onReceive(context, intent)
    }

    /**
     * Called when message is received. The message contains the full POST body of the push message.
     *
     * @param context the Android context
     * @param message the message
     * @param instance connection, for multi-account
     */
    override fun onMessage(context: Context, message: PushMessage, instance: String) {
        Timber.tag(loggerTag.value).d("New message, decrypted: ${message.decrypted}")
        coroutineScope.launch {
            val pushData = pushParser.parse(message.content, instance)
            if (pushData == null) {
                Timber.tag(loggerTag.value).w("Invalid data received from UnifiedPush")
                pushHandler.handleInvalid(
                    providerInfo = "${UnifiedPushConfig.NAME} - $instance",
                    data = String(message.content),
                )
            } else {
                pushHandler.handle(
                    pushData = pushData,
                    providerInfo = "${UnifiedPushConfig.NAME} - $instance",
                )
            }
        }
    }

    /**
     * Called when a new endpoint is to be used for sending push messages.
     * You should send the endpoint to your application server and sync for missing notifications.
     */
    override fun onNewEndpoint(context: Context, endpoint: PushEndpoint, instance: String) {
        Timber.tag(loggerTag.value).w("onNewEndpoint: $endpoint")
        coroutineScope.launch {
            val gateway = unifiedPushGatewayResolver.getGateway(endpoint.url)
                .let { gatewayResult ->
                    unifiedPushGatewayUrlResolver.resolve(gatewayResult, instance)
                }
            unifiedPushStore.storePushGateway(instance, gateway)
            val result = newGatewayHandler.handle(endpoint.url, gateway, instance)
                .onFailure {
                    Timber.tag(loggerTag.value).e(it, "Failed to handle new gateway")
                }
                .onSuccess {
                    unifiedPushStore.storeUpEndpoint(instance, endpoint.url)
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
    override fun onRegistrationFailed(context: Context, reason: FailedReason, instance: String) {
        Timber.tag(loggerTag.value).e("onRegistrationFailed for $instance, reason: $reason")
        coroutineScope.launch {
            endpointRegistrationHandler.registrationDone(
                RegistrationResult(
                    clientSecret = instance,
                    result = Result.failure(Exception("Registration failed. Reason: $reason")),
                )
            )
        }
    }

    /**
     * Called when this application is unregistered from receiving push messages.
     */
    override fun onUnregistered(context: Context, instance: String) {
        Timber.tag(loggerTag.value).w("onUnregistered $instance")
        coroutineScope.launch {
            removedGatewayHandler.handle(instance)
        }
    }
}
