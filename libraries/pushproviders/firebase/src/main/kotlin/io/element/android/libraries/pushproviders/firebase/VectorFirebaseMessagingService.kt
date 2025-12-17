/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.pushproviders.api.PushHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

private val loggerTag = LoggerTag("VectorFirebaseMessagingService", LoggerTag.PushLoggerTag)

class VectorFirebaseMessagingService : FirebaseMessagingService() {
    @Inject lateinit var firebaseNewTokenHandler: FirebaseNewTokenHandler
    @Inject lateinit var pushParser: FirebasePushParser
    @Inject lateinit var pushHandler: PushHandler
    @AppCoroutineScope
    @Inject lateinit var coroutineScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        bindings<VectorFirebaseMessagingServiceBindings>().inject(this)
    }

    override fun onNewToken(token: String) {
        Timber.tag(loggerTag.value).w("New Firebase token")
        coroutineScope.launch {
            firebaseNewTokenHandler.handle(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Timber.tag(loggerTag.value).w("New Firebase message. Priority: ${message.priority}/${message.originalPriority}")
        coroutineScope.launch {
            val pushData = pushParser.parse(message.data)
            if (pushData == null) {
                Timber.tag(loggerTag.value).w("Invalid data received from Firebase")
                pushHandler.handleInvalid(
                    providerInfo = FirebaseConfig.NAME,
                    data = message.data.keys.joinToString("\n") {
                        "$it: ${message.data[it]}"
                    },
                )
            } else {
                pushHandler.handle(
                    pushData = pushData,
                    providerInfo = FirebaseConfig.NAME,
                )
            }
        }
    }
}
