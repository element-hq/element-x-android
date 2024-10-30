/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase

import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface FirebaseTokenGetter {
    suspend fun get(): String
}

/**
 * This class read the current Firebase token.
 * If the token does not exist, it will be generated.
 */
@ContributesBinding(AppScope::class)
class DefaultFirebaseTokenGetter @Inject constructor(
    private val isPlayServiceAvailable: IsPlayServiceAvailable,
) : FirebaseTokenGetter {
    override suspend fun get(): String {
        return suspendCoroutine { continuation ->
            // 'app should always check the device for a compatible Google Play services APK before accessing Google Play services features'
            if (isPlayServiceAvailable.isAvailable()) {
                try {
                    FirebaseMessaging.getInstance().token
                        .addOnSuccessListener { token ->
                            continuation.resume(token)
                        }
                        .addOnFailureListener { e ->
                            Timber.e(e, "## retrievedFirebaseToken() : failed")
                            continuation.resumeWithException(e)
                        }
                } catch (e: Throwable) {
                    Timber.e(e, "## retrievedFirebaseToken() : failed")
                    continuation.resumeWithException(e)
                }
            } else {
                val e = Exception("No valid Google Play Services found. Cannot use FCM.")
                Timber.e(e)
                continuation.resumeWithException(e)
            }
        }
    }
}
