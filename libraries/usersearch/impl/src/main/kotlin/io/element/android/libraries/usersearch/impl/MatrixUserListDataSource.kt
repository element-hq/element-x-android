/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.usersearch.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.usersearch.api.UserListDataSource
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class MatrixUserListDataSource @Inject constructor(
    private val client: MatrixClient
) : UserListDataSource {
    override suspend fun search(query: String, count: Long): List<MatrixUser> {
        val res = client.searchUsers(query, count)
        return res.getOrNull()?.results.orEmpty()
    }

    override suspend fun getProfile(userId: UserId): MatrixUser? {
        return client.getProfile(userId).getOrNull()
    }
}
