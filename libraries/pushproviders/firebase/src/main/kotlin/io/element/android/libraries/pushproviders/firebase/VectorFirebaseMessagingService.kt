/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.pushproviders.api.PushHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private val loggerTag = LoggerTag("VectorFirebaseMessagingService", LoggerTag.PushLoggerTag)

class VectorFirebaseMessagingService : FirebaseMessagingService() {
    @Inject lateinit var firebaseNewTokenHandler: FirebaseNewTokenHandler
    @Inject lateinit var pushParser: FirebasePushParser
    @Inject lateinit var pushHandler: PushHandler
    @Inject lateinit var coroutineScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        applicationContext.bindings<VectorFirebaseMessagingServiceBindings>().inject(this)
    }

    override fun onNewToken(token: String) {
        Timber.tag(loggerTag.value).d("New Firebase token")
        coroutineScope.launch {
            firebaseNewTokenHandler.handle(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Timber.tag(loggerTag.value).d("New Firebase message")
        coroutineScope.launch {
            val pushData = pushParser.parse(message.data)
            if (pushData == null) {
                Timber.tag(loggerTag.value).w("Invalid data received from Firebase")
            } else {
                pushHandler.handle(pushData)
            }
        }
    }
}
