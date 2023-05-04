/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.createroom.impl

import io.element.android.features.userlist.api.UserListDataSource
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import javax.inject.Inject

class AllMatrixUsersDataSource @Inject constructor(
    private val client: MatrixClient
) : UserListDataSource {
    override suspend fun search(query: String): List<MatrixUser> {
        val res = client.searchUsers(query, MAX_SEARCH_RESULTS)
        return res.getOrNull()?.results.orEmpty()
    }

    override suspend fun getProfile(userId: UserId): MatrixUser? {
        // TODO hook up to matrix client
        return null
    }

    companion object {
        private const val MAX_SEARCH_RESULTS = 5L
    }
}
