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

package io.element.android.libraries.usersearch.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.MatrixPatterns
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.usersearch.api.UserListDataSource
import io.element.android.libraries.usersearch.api.UserRepository
import io.element.android.libraries.usersearch.api.UserSearchResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class MatrixUserRepository @Inject constructor(
    private val dataSource: UserListDataSource
) : UserRepository {

    override suspend fun search(query: String): Flow<List<UserSearchResult>> = flow {
        // Manually add a fake result with the matrixId, if any
        val isUserId = MatrixPatterns.isUserId(query)
        if (isUserId) {
            emit(listOf(UserSearchResult(MatrixUser(UserId(query)))))
        }

        if (query.length >= MINIMUM_SEARCH_LENGTH) {
            // Debounce
            delay(DEBOUNCE_TIME_MILLIS)

            val results = dataSource.search(query, MAXIMUM_SEARCH_RESULTS).map { UserSearchResult(it) }.toMutableList()

            // If the query is a user ID and the result doesn't contain that user ID, query the profile information explicitly
            if (isUserId && results.none { it.matrixUser.userId.value == query }) {
                results.add(
                    0,
                    dataSource.getProfile(UserId(query))
                        ?.let { UserSearchResult(it) }
                        ?: UserSearchResult(MatrixUser(UserId(query)), isUnresolved = true))
            }

            emit(results)
        }
    }

    companion object {
        private const val DEBOUNCE_TIME_MILLIS = 250L
        private const val MINIMUM_SEARCH_LENGTH = 3
        private const val MAXIMUM_SEARCH_RESULTS = 10L
    }
}
