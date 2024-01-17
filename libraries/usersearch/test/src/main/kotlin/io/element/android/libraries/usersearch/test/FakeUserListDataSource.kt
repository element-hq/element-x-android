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

package io.element.android.libraries.usersearch.test

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.usersearch.api.UserListDataSource

class FakeUserListDataSource : UserListDataSource {
    private var searchResult: List<MatrixUser> = emptyList()
    private var profile: MatrixUser? = null

    override suspend fun search(query: String, count: Long): List<MatrixUser> = searchResult.take(count.toInt())

    override suspend fun getProfile(userId: UserId): MatrixUser? = profile

    fun givenSearchResult(users: List<MatrixUser>) {
        this.searchResult = users
    }

    fun givenUserProfile(matrixUser: MatrixUser?) {
        this.profile = matrixUser
    }
}
