/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.feral

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import io.element.android.appconfig.FeralPushConfig
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushproviders.api.Config
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushproviders.api.PusherSubscriber
import io.element.android.libraries.pushproviders.feral.service.FeralPushServiceController
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import kotlinx.coroutines.flow.first
import timber.log.Timber

private val loggerTag = LoggerTag("FeralPushProvider", LoggerTag.PushLoggerTag)

/**
 * Built-in push provider: no distributor app needed. The pusher targets the Feral ntfy server
 * (gateway) with a per-session topic, and [FeralPushServiceController] keeps a WebSocket to that
 * topic open from a foreground service.
 */
@ContributesIntoSet(AppScope::class)
class FeralPushProvider(
    private val pusherSubscriber: PusherSubscriber,
    private val pushClientSecret: PushClientSecret,
    private val feralPushStore: FeralPushStore,
    private val topicGenerator: FeralPushTopicGenerator,
    private val userPushStoreFactory: UserPushStoreFactory,
    private val serviceController: FeralPushServiceController,
) : PushProvider {
    override val index = FeralPushConfig.INDEX
    override val name = FeralPushConfig.NAME
    override val supportMultipleDistributors = false

    override fun getDistributors(): List<Distributor> = listOf(distributor)

    override suspend fun registerWith(matrixClient: MatrixClient, distributor: Distributor): Result<Unit> {
        val sessionId = matrixClient.sessionId
        val existing = feralPushStore.get(sessionId)
        val topic = existing?.topic ?: topicGenerator.generate()
        val endpoint = endpointFor(topic)
        val clientSecret = pushClientSecret.getSecretForUser(sessionId)
        return pusherSubscriber.registerPusher(matrixClient, pushKey = endpoint, gateway = FeralPushConfig.GATEWAY_URL)
            .onSuccess {
                feralPushStore.set(
                    FeralPushRegistration(
                        sessionId = sessionId.value,
                        topic = topic,
                        endpoint = endpoint,
                        clientSecret = clientSecret,
                        // Keep the replay cursor when re-registering the same topic.
                        lastMessageId = existing?.lastMessageId?.takeIf { existing.topic == topic },
                    )
                )
                userPushStoreFactory.getOrCreate(sessionId).setCurrentRegisteredPushKey(endpoint)
                serviceController.ensureStarted()
            }
            .onFailure {
                Timber.tag(loggerTag.value).e(it, "Unable to register the Feral pusher")
            }
    }

    override suspend fun getCurrentDistributorValue(sessionId: SessionId): String? {
        return feralPushStore.get(sessionId)?.let { distributor.value }
    }

    override suspend fun getCurrentDistributor(sessionId: SessionId): Distributor? {
        return feralPushStore.get(sessionId)?.let { distributor }
    }

    override suspend fun unregister(matrixClient: MatrixClient): Result<Unit> {
        val sessionId = matrixClient.sessionId
        val registration = feralPushStore.get(sessionId)
        if (registration == null) {
            Timber.tag(loggerTag.value).w("No Feral registration found for the session")
            return Result.success(Unit)
        }
        return pusherSubscriber.unregisterPusher(matrixClient, pushKey = registration.endpoint, gateway = FeralPushConfig.GATEWAY_URL)
            .recoverCatching { throwable ->
                if (throwable.isPusherNotFound()) {
                    Timber.tag(loggerTag.value).w("Pusher already gone from the homeserver, ignoring")
                } else {
                    throw throwable
                }
            }
            .onSuccess { cleanup(sessionId) }
    }

    override suspend fun onSessionDeleted(sessionId: SessionId) {
        cleanup(sessionId)
    }

    override suspend fun getPushConfig(sessionId: SessionId): Config? {
        return feralPushStore.get(sessionId)?.let { Config(url = FeralPushConfig.GATEWAY_URL, pushKey = it.endpoint) }
    }

    override fun canRotateToken(): Boolean = false

    private suspend fun cleanup(sessionId: SessionId) {
        feralPushStore.remove(sessionId)
        if (feralPushStore.registrations.first().isEmpty()) {
            serviceController.stop()
        }
    }

    private fun Throwable.isPusherNotFound(): Boolean {
        val text = (message.orEmpty() + " " + (cause?.message).orEmpty()).lowercase()
        return "m_not_found" in text || "not found" in text || "404" in text
    }

    companion object {
        val distributor = Distributor(value = FeralPushConfig.DISTRIBUTOR_VALUE, name = FeralPushConfig.DISTRIBUTOR_NAME)

        fun endpointFor(topic: String): String = "${FeralPushConfig.SERVER_URL}/$topic?up=1"
    }
}
