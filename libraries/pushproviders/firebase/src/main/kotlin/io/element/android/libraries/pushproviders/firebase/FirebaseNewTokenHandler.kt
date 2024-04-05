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

import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushproviders.api.PusherSubscriber
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.api.toUserList
import timber.log.Timber
import javax.inject.Inject

private val loggerTag = LoggerTag("FirebaseNewTokenHandler", LoggerTag.PushLoggerTag)

/**
 * Handle new token receive from Firebase. Will update all the sessions which are using Firebase as a push provider.
 */
class FirebaseNewTokenHandler @Inject constructor(
    private val pusherSubscriber: PusherSubscriber,
    private val sessionStore: SessionStore,
    private val userPushStoreFactory: UserPushStoreFactory,
    private val matrixAuthenticationService: MatrixAuthenticationService,
    private val firebaseStore: FirebaseStore,
) {
    suspend fun handle(firebaseToken: String) {
        firebaseStore.storeFcmToken(firebaseToken)
        // Register the pusher for all the sessions
        sessionStore.getAllSessions().toUserList()
            .map { SessionId(it) }
            .forEach { userId ->
                val userDataStore = userPushStoreFactory.getOrCreate(userId)
                if (userDataStore.getPushProviderName() == FirebaseConfig.NAME) {
                    matrixAuthenticationService.restoreSession(userId).getOrNull()?.use { client ->
                        pusherSubscriber.registerPusher(client, firebaseToken, FirebaseConfig.PUSHER_HTTP_URL)
                    }
                } else {
                    Timber.tag(loggerTag.value).d("This session is not using Firebase pusher")
                }
            }
    }
}
