/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase

import com.squareup.anvil.annotations.ContributesMultibinding
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushproviders.api.CurrentUserPushConfig
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushproviders.api.PusherSubscriber
import timber.log.Timber
import javax.inject.Inject

private val loggerTag = LoggerTag("FirebasePushProvider", LoggerTag.PushLoggerTag)

@ContributesMultibinding(AppScope::class)
class FirebasePushProvider @Inject constructor(
    private val firebaseStore: FirebaseStore,
    private val pusherSubscriber: PusherSubscriber,
    private val isPlayServiceAvailable: IsPlayServiceAvailable,
    private val firebaseTokenRotator: FirebaseTokenRotator,
    private val firebaseGatewayProvider: FirebaseGatewayProvider,
) : PushProvider {
    override val index = FirebaseConfig.INDEX
    override val name = FirebaseConfig.NAME

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

    override suspend fun getCurrentUserPushConfig(): CurrentUserPushConfig? {
        return firebaseStore.getFcmToken()?.let { fcmToken ->
            CurrentUserPushConfig(
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
