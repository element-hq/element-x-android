/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushproviders.api.Config
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushproviders.api.PusherSubscriber
import timber.log.Timber

private val loggerTag = LoggerTag("FirebasePushProvider", LoggerTag.PushLoggerTag)

@ContributesIntoSet(AppScope::class)
@Inject
class FirebasePushProvider(
    private val firebaseStore: FirebaseStore,
    private val pusherSubscriber: PusherSubscriber,
    private val isPlayServiceAvailable: IsPlayServiceAvailable,
    private val firebaseTokenRotator: FirebaseTokenRotator,
    private val firebaseGatewayProvider: FirebaseGatewayProvider,
) : PushProvider {
    override val index = FirebaseConfig.INDEX
    override val name = FirebaseConfig.NAME
    override val supportMultipleDistributors = false

    override fun getDistributors(): List<Distributor> {
        return listOfNotNull(
            firebaseDistributor.takeIf { isPlayServiceAvailable.isAvailable() }
        )
    }

    override suspend fun registerWith(matrixClient: MatrixClient, distributor: Distributor): Result<Unit> {
        val pushKey = firebaseStore.getFcmToken() ?: return Result.failure<Unit>(
            IllegalStateException(
                "Unable to register pusher, Firebase token is not known."
            )
        ).also {
            Timber.tag(loggerTag.value).w("Unable to register pusher, Firebase token is not known.")
        }
        return pusherSubscriber.registerPusher(
            matrixClient = matrixClient,
            pushKey = pushKey,
            gateway = firebaseGatewayProvider.getFirebaseGateway(),
        )
    }

    override suspend fun getCurrentDistributorValue(sessionId: SessionId): String = firebaseDistributor.value

    override suspend fun getCurrentDistributor(sessionId: SessionId) = firebaseDistributor

    override suspend fun unregister(matrixClient: MatrixClient): Result<Unit> {
        val pushKey = firebaseStore.getFcmToken()
        return if (pushKey == null) {
            Timber.tag(loggerTag.value).w("Unable to unregister pusher, Firebase token is not known.")
            Result.success(Unit)
        } else {
            pusherSubscriber.unregisterPusher(matrixClient, pushKey, firebaseGatewayProvider.getFirebaseGateway())
        }
    }

    /**
     * Nothing to clean up here.
     */
    override suspend fun onSessionDeleted(sessionId: SessionId) = Unit

    override suspend fun getPushConfig(sessionId: SessionId): Config? {
        return firebaseStore.getFcmToken()?.let { fcmToken ->
            Config(
                url = firebaseGatewayProvider.getFirebaseGateway(),
                pushKey = fcmToken
            )
        }
    }

    override fun canRotateToken(): Boolean = true

    override suspend fun rotateToken(): Result<Unit> {
        return firebaseTokenRotator.rotate()
    }

    companion object {
        private val firebaseDistributor = Distributor("Firebase", "Firebase")
    }
}
