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

import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface FirebaseTroubleshooter {
    suspend fun troubleshoot(): Result<Unit>
}

/**
 * This class force retrieving and storage of the Firebase token.
 */
@ContributesBinding(AppScope::class)
class DefaultFirebaseTroubleshooter @Inject constructor(
    private val newTokenHandler: FirebaseNewTokenHandler,
    private val isPlayServiceAvailable: IsPlayServiceAvailable,
) : FirebaseTroubleshooter {
    override suspend fun troubleshoot(): Result<Unit> {
        return runCatching {
            val token = retrievedFirebaseToken()
            newTokenHandler.handle(token)
        }
    }

    private suspend fun retrievedFirebaseToken(): String {
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
