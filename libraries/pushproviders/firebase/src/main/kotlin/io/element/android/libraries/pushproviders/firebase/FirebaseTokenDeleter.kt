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
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface FirebaseTokenDeleter {
    /**
     * Deletes the current Firebase token.
     */
    suspend fun delete()
}

@ContributesBinding(AppScope::class)
class DefaultFirebaseTokenDeleter(
    private val isPlayServiceAvailable: IsPlayServiceAvailable,
) : FirebaseTokenDeleter {
    override suspend fun delete() {
        // 'app should always check the device for a compatible Google Play services APK before accessing Google Play services features'
        isPlayServiceAvailable.checkAvailableOrThrow()
        suspendCoroutine { continuation ->
            try {
                FirebaseMessaging.getInstance().deleteToken()
                    .addOnSuccessListener {
                        continuation.resume(Unit)
                    }
                    .addOnFailureListener { e ->
                        Timber.e(e, "## deleteFirebaseToken() : failed")
                        continuation.resumeWithException(e)
                    }
            } catch (e: Throwable) {
                Timber.e(e, "## deleteFirebaseToken() : failed")
                continuation.resumeWithException(e)
            }
        }
    }
}
