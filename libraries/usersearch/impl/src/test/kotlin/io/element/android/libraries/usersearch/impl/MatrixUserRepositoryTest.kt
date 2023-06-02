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

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import io.element.android.libraries.usersearch.api.UserSearchResult
import io.element.android.libraries.usersearch.test.FakeUserListDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class MatrixUserRepositoryTest {

    @Test
    fun `search - emits nothing if the search query is too short`() = runTest {
        val dataSource = FakeUserListDataSource()
        val repository = MatrixUserRepository(dataSource)

        val result = repository.search("x")

        result.test {
            awaitComplete()
        }
    }

    @Test
    fun `search - returns empty list if no results are found`() = runTest {
        val dataSource = FakeUserListDataSource()
        val repository = MatrixUserRepository(dataSource)

        val result = repository.search("some query")

        result.test {
            assertThat(awaitItem()).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `search - returns users if results are found`() = runTest {
        val dataSource = FakeUserListDataSource()
        dataSource.givenSearchResult(aMatrixUserList())
        val repository = MatrixUserRepository(dataSource)

        val result = repository.search("some query")

        result.test {
            assertThat(awaitItem()).isEqualTo(aMatrixUserList().toUserSearchResults())
            awaitComplete()
        }
    }

    @Test
    fun `search - immediately returns placeholder if search is mxid`() = runTest {
        val dataSource = FakeUserListDataSource()
        val repository = MatrixUserRepository(dataSource)

        val result = repository.search(A_USER_ID.value)

        result.test {
            assertThat(awaitItem()).isEqualTo(listOf(placeholderResult()))
            skipItems(1)
            awaitComplete()
        }
    }

    @Test
    fun `search - does not change results if they contain searched mxid`() = runTest {
        val searchResults = aMatrixUserListWithoutUserId(A_USER_ID) + MatrixUser(userId = A_USER_ID, displayName = A_USER_NAME)
        val dataSource = FakeUserListDataSource()
        dataSource.givenSearchResult(searchResults)
        val repository = MatrixUserRepository(dataSource)

        val result = repository.search(A_USER_ID.value)

        result.test {
            skipItems(1)
            assertThat(awaitItem()).isEqualTo(searchResults.toUserSearchResults())
            awaitComplete()
        }
    }

    @Test
    fun `search - gets profile results if searched mxid not in results`() = runTest {
        val userProfile = MatrixUser(userId = A_USER_ID, displayName = A_USER_NAME)
        val searchResults = aMatrixUserListWithoutUserId(A_USER_ID)

        val dataSource = FakeUserListDataSource()
        dataSource.givenSearchResult(searchResults)
        dataSource.givenUserProfile(userProfile)
        val repository = MatrixUserRepository(dataSource)

        val result = repository.search(A_USER_ID.value)

        result.test {
            skipItems(1)
            assertThat(awaitItem()).isEqualTo((listOf(userProfile) + searchResults).toUserSearchResults())
            awaitComplete()
        }
    }

    @Test
    fun `search - returns unresolved user if profile can't be loaded`() = runTest {
        val searchResults = aMatrixUserListWithoutUserId(A_USER_ID)

        val dataSource = FakeUserListDataSource()
        dataSource.givenSearchResult(searchResults)
        dataSource.givenUserProfile(null)
        val repository = MatrixUserRepository(dataSource)

        val result = repository.search(A_USER_ID.value)

        result.test {
            skipItems(1)
            assertThat(awaitItem()).isEqualTo(listOf(placeholderResult(isUnresolved = true)) + searchResults.toUserSearchResults())
            awaitComplete()
        }
    }

    private fun aMatrixUserListWithoutUserId(userId: UserId) = aMatrixUserList().filterNot { it.userId == userId }

    private fun List<MatrixUser>.toUserSearchResults() = map { UserSearchResult(it) }

    private fun placeholderResult(id: UserId = A_USER_ID, isUnresolved: Boolean = false) = UserSearchResult(MatrixUser(id), isUnresolved = isUnresolved)

}
