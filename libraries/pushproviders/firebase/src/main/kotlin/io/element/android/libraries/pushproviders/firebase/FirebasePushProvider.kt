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

package io.element.android.libraries.pushproviders.firebase

import com.squareup.anvil.annotations.ContributesMultibinding
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.MatrixClient
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
) : PushProvider {
    override val index = FirebaseConfig.INDEX
    override val name = FirebaseConfig.NAME

    override fun isAvailable(): Boolean {
        return isPlayServiceAvailable.isAvailable()
    }

    override fun getDistributors(): List<Distributor> {
        return listOf(firebaseDistributor)
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
            gateway = FirebaseConfig.PUSHER_HTTP_URL,
        )
    }

    override suspend fun getCurrentDistributor(matrixClient: MatrixClient) = firebaseDistributor

    override suspend fun unregister(matrixClient: MatrixClient): Result<Unit> {
        val pushKey = firebaseStore.getFcmToken()
        return if (pushKey == null) {
            Timber.tag(loggerTag.value).w("Unable to unregister pusher, Firebase token is not known.")
            Result.success(Unit)
        } else {
            pusherSubscriber.unregisterPusher(matrixClient, pushKey, FirebaseConfig.PUSHER_HTTP_URL)
        }
    }

    override suspend fun getCurrentUserPushConfig(): CurrentUserPushConfig? {
        return firebaseStore.getFcmToken()?.let { fcmToken ->
            CurrentUserPushConfig(
                url = FirebaseConfig.PUSHER_HTTP_URL,
                pushKey = fcmToken
            )
        }
    }

    companion object {
        private val firebaseDistributor = Distributor("Firebase", "Firebase")
    }
}
