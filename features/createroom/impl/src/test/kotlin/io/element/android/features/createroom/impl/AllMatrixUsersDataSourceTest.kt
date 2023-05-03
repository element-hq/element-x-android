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

import com.google.common.truth.Truth
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.usersearch.MatrixSearchUserResults
import io.element.android.libraries.matrix.api.usersearch.MatrixUserProfile
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.ui.model.MatrixUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class AllMatrixUsersDataSourceTest {

    @Test
    fun `search - returns users on success`() = runTest {
        val matrixClient = FakeMatrixClient()
        matrixClient.givenSearchUsersResult(
            searchTerm = "test",
            result = Result.success(
                MatrixSearchUserResults(
                    results = listOf(aMatrixUserProfile(), aMatrixUserProfile(userId = A_USER_ID_2)),
                    limited = false
                )
            )
        )
        val dataSource = AllMatrixUsersDataSource(matrixClient)

        val results = dataSource.search("test")
        Truth.assertThat(results).containsExactly(
            MatrixUser(
                id = A_USER_ID,
                username = A_USER_NAME,
                avatarData = AvatarData(id = A_USER_ID.value, name = A_USER_NAME, url = AN_AVATAR_URL)
            ),
            MatrixUser(
                id = A_USER_ID_2,
                username = A_USER_NAME,
                avatarData = AvatarData(id = A_USER_ID_2.value, name = A_USER_NAME, url = AN_AVATAR_URL)
            )
        )
    }

    @Test
    fun `search - returns empty list on error`() = runTest {
        val matrixClient = FakeMatrixClient()
        matrixClient.givenSearchUsersResult(
            searchTerm = "test",
            result = Result.failure(Throwable("Ruhroh"))
        )
        val dataSource = AllMatrixUsersDataSource(matrixClient)

        val results = dataSource.search("test")
        Truth.assertThat(results).isEmpty()
    }

    private fun aMatrixUserProfile(
        userId: UserId = A_USER_ID,
        displayName: String = A_USER_NAME,
        avatarUrl: String = AN_AVATAR_URL
    ) = MatrixUserProfile(userId, displayName, avatarUrl)
}
