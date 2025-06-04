/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

interface FirebaseTroubleshooter {
    suspend fun troubleshoot(): Result<Unit>
}

/**
 * This class force retrieving and storage of the Firebase token.
 */
@ContributesBinding(AppScope::class)
class DefaultFirebaseTroubleshooter @Inject constructor(
    private val newTokenHandler: FirebaseNewTokenHandler,
    private val firebaseTokenGetter: FirebaseTokenGetter,
) : FirebaseTroubleshooter {
    override suspend fun troubleshoot(): Result<Unit> {
        return runCatchingExceptions {
            val token = firebaseTokenGetter.get()
            newTokenHandler.handle(token)
        }
    }
}
