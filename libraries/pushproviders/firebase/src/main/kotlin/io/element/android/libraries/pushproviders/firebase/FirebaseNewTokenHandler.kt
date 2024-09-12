/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.extensions.flatMap
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.MatrixClientProvider
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
interface FirebaseNewTokenHandler {
    suspend fun handle(firebaseToken: String)
}

@ContributesBinding(AppScope::class)
class DefaultFirebaseNewTokenHandler @Inject constructor(
    private val pusherSubscriber: PusherSubscriber,
    private val sessionStore: SessionStore,
    private val userPushStoreFactory: UserPushStoreFactory,
    private val matrixClientProvider: MatrixClientProvider,
    private val firebaseStore: FirebaseStore,
) : FirebaseNewTokenHandler {
    override suspend fun handle(firebaseToken: String) {
        firebaseStore.storeFcmToken(firebaseToken)
        // Register the pusher for all the sessions
        sessionStore.getAllSessions().toUserList()
            .map { SessionId(it) }
            .forEach { sessionId ->
                val userDataStore = userPushStoreFactory.getOrCreate(sessionId)
                if (userDataStore.getPushProviderName() == FirebaseConfig.NAME) {
                    matrixClientProvider
                        .getOrRestore(sessionId)
                        .onFailure {
                            Timber.tag(loggerTag.value).e(it, "Failed to restore session $sessionId")
                        }
                        .flatMap { client ->
                            pusherSubscriber
                                .registerPusher(
                                    matrixClient = client,
                                    pushKey = firebaseToken,
                                    gateway = FirebaseConfig.PUSHER_HTTP_URL,
                                )
                                .onFailure {
                                    Timber.tag(loggerTag.value).e(it, "Failed to register pusher for session $sessionId")
                                }
                        }
                } else {
                    Timber.tag(loggerTag.value).d("This session is not using Firebase pusher")
                }
            }
    }
}
