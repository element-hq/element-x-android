/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushstore.test.userpushstore

import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushstore.api.UserPushStore
import io.element.android.libraries.pushstore.api.UserPushStoreFactory

class FakeUserPushStoreFactory(
    val userPushStore: (SessionId) -> UserPushStore = { FakeUserPushStore() }
) : UserPushStoreFactory {
    override fun getOrCreate(userId: SessionId): UserPushStore {
        return userPushStore(userId)
    }
}
