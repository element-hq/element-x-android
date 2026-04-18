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
import com.google.firebase.messaging.RemoteMessage.PRIORITY_HIGH
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.push.api.push.PushHandlingWakeLock
import io.element.android.libraries.pushproviders.api.PushHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

private val loggerTag = LoggerTag("VectorFirebaseMessagingService", LoggerTag.PushLoggerTag)

class VectorFirebaseMessagingService : FirebaseMessagingService() {
    @Inject lateinit var firebaseNewTokenHandler: FirebaseNewTokenHandler
    @Inject lateinit var pushParser: FirebasePushParser
    @Inject lateinit var pushHandler: PushHandler
    @Inject lateinit var pushHandlingWakeLock: PushHandlingWakeLock
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

        val isHighPriority = message.priority == PRIORITY_HIGH
        if (isHighPriority) {
            // Acquire wakelock to ensure the device stays awake while we handle the push and schedule and run the work
            pushHandlingWakeLock.lock()
        }

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
                if (isHighPriority) {
                    pushHandlingWakeLock.unlock()
                }
            } else {
                val handled = pushHandler.handle(
                    pushData = pushData,
                    providerInfo = FirebaseConfig.NAME,
                )

                // If we failed to handle the push, we should release the wakelock early to avoid keeping the device awake for too long.
                if (!handled && isHighPriority) {
                    pushHandlingWakeLock.unlock()
                }
            }
        }
    }
}
