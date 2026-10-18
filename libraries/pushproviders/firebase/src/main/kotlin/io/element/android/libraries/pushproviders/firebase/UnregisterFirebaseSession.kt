/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase

import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.pushproviders.firebase.util.runFirebaseTask
import timber.log.Timber

fun interface UnregisterFirebaseSession {
    /**
     * Deletes the current Firebase token.
     */
    suspend operator fun invoke(): Result<Unit>
}

@ContributesBinding(AppScope::class)
class DefaultUnregisterFirebaseSession(
    private val isPlayServiceAvailable: IsPlayServiceAvailable,
) : UnregisterFirebaseSession {
    override suspend operator fun invoke(): Result<Unit> {
        // 'app should always check the device for a compatible Google Play services APK before accessing Google Play services features'
        isPlayServiceAvailable.checkAvailableOrThrow()
        return runFirebaseTask {
            // Unregister the device from Firebase Messaging
            FirebaseMessaging.getInstance().unregister()
                // Also delete the existing installation id from Firebase
                .continueWithTask { FirebaseInstallations.getInstance().delete() }
        }
            .onFailure {
                Timber.e(it, "## unregisterFirebaseMessaging() : failed")
            }
            // Change return type from Void! to Unit
            .map {}
    }
}
