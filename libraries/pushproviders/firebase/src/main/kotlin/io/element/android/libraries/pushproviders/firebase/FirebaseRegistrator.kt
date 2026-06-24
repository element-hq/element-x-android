/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase

import com.google.firebase.messaging.FirebaseMessaging
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun interface FirebaseRegistrator {
    /**
     * Register the device to Firebase Messaging.
     */
    suspend fun get()
}

@ContributesBinding(AppScope::class)
class DefaultFirebaseRegistrator(
    private val isPlayServiceAvailable: IsPlayServiceAvailable,
) : FirebaseRegistrator {
    override suspend fun get() {
        // 'app should always check the device for a compatible Google Play services APK before accessing Google Play services features'
        isPlayServiceAvailable.checkAvailableOrThrow()
        return suspendCancellableCoroutine { continuation ->
            try {
                FirebaseMessaging.getInstance().register()
                    .addOnSuccessListener {
                        continuation.resume(Unit)
                    }
                    .addOnFailureListener { e ->
                        Timber.e(e, "## registerFirebaseMessaging() : failed")
                        continuation.resumeWithException(e)
                    }
            } catch (e: Throwable) {
                Timber.e(e, "## registerFirebaseMessaging() : failed")
                continuation.resumeWithException(e)
            }
        }
    }
}
