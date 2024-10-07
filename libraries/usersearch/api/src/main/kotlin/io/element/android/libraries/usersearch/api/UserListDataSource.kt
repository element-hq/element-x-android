/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.usersearch.api

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser

interface UserListDataSource {
    // TODO should probably have a flow
    suspend fun search(query: String, count: Long): List<MatrixUser>
    suspend fun getProfile(userId: UserId): MatrixUser?
}
