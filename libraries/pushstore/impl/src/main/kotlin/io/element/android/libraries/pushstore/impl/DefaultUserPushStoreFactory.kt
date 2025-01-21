/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushstore.impl

import android.content.Context
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushstore.api.UserPushStore
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultUserPushStoreFactory @Inject constructor(
    @ApplicationContext private val context: Context,
) : UserPushStoreFactory {
    // We can have only one class accessing a single data store, so keep a cache of them.
    private val cache = ConcurrentHashMap<SessionId, UserPushStore>()
    override fun getOrCreate(userId: SessionId): UserPushStore {
        return cache.getOrPut(userId) {
            UserPushStoreDataStore(
                context = context,
                userId = userId
            )
        }
    }
}
