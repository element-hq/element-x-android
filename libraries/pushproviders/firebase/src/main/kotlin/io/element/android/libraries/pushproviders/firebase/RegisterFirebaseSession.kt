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
import io.element.android.libraries.pushproviders.firebase.util.runFirebaseTask
import timber.log.Timber

fun interface RegisterFirebaseSession {
    /**
     * Register the device to Firebase Messaging.
     */
    suspend operator fun invoke(): Result<Unit>
}

@ContributesBinding(AppScope::class)
class DefaultRegisterFirebaseSession(
    private val isPlayServiceAvailable: IsPlayServiceAvailable,
) : RegisterFirebaseSession {
    override suspend operator fun invoke(): Result<Unit> {
        // 'app should always check the device for a compatible Google Play services APK before accessing Google Play services features'
        isPlayServiceAvailable.checkAvailableOrThrow()
        return runFirebaseTask { FirebaseMessaging.getInstance().register() }
            .onFailure { Timber.e(it, "## registerFirebaseMessaging() : failed") }
            // Change return type from Void! to Unit
            .map {}
    }
}
