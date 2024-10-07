/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushstore.api

import io.element.android.libraries.matrix.api.core.SessionId

/**
 * Store data related to push about a user.
 */
interface UserPushStoreFactory {
    fun getOrCreate(userId: SessionId): UserPushStore
}
