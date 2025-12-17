/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.extensions.runCatchingExceptions

interface FirebaseTokenRotator {
    suspend fun rotate(): Result<Unit>
}

/**
 * This class delete the Firebase token and generate a new one.
 */
@ContributesBinding(AppScope::class)
class DefaultFirebaseTokenRotator(
    private val firebaseTokenDeleter: FirebaseTokenDeleter,
    private val firebaseTokenGetter: FirebaseTokenGetter,
) : FirebaseTokenRotator {
    override suspend fun rotate(): Result<Unit> {
        return runCatchingExceptions {
            firebaseTokenDeleter.delete()
            firebaseTokenGetter.get()
        }
    }
}
