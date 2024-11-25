/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

interface FirebaseTokenRotator {
    suspend fun rotate(): Result<Unit>
}

/**
 * This class delete the Firebase token and generate a new one.
 */
@ContributesBinding(AppScope::class)
class DefaultFirebaseTokenRotator @Inject constructor(
    private val firebaseTokenDeleter: FirebaseTokenDeleter,
    private val firebaseTokenGetter: FirebaseTokenGetter,
) : FirebaseTokenRotator {
    override suspend fun rotate(): Result<Unit> {
        return runCatching {
            firebaseTokenDeleter.delete()
            firebaseTokenGetter.get()
        }
    }
}
