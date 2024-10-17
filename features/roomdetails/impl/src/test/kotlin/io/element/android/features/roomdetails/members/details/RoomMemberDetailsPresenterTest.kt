/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.members.details

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.roomdetails.aMatrixRoom
import io.element.android.features.roomdetails.impl.members.aRoomMember
import io.element.android.features.roomdetails.impl.members.details.RoomMemberDetailsPresenter
import io.element.android.features.userprofile.api.UserProfilePresenterFactory
import io.element.android.features.userprofile.shared.aUserProfileState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class RoomMemberDetailsPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - returns the room member's data, then updates it if needed`() = runTest {
        val roomMember = aRoomMember(displayName = "Alice")
        val room = aMatrixRoom(
            userDisplayNameResult = { Result.success("A custom name") },
            userAvatarUrlResult = { Result.success("A custom avatar") },
            getUpdatedMemberResult = { Result.success(roomMember) },
        ).apply {
            givenRoomMembersState(MatrixRoomMembersState.Ready(persistentListOf(roomMember)))
        }
        val presenter = createRoomMemberDetailsPresenter(
            room = room,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.userName).isEqualTo("Alice")
            assertThat(initialState.avatarUrl).isEqualTo("Profile avatar url")
            skipItems(1)
            val nextState = awaitItem()
            assertThat(nextState.userName).isEqualTo("A custom name")
            assertThat(nextState.avatarUrl).isEqualTo("A custom avatar")
        }
    }

    @Test
    fun `present - will recover when retrieving room member details fails`() = runTest {
        val roomMember = aRoomMember(
            displayName = "Alice",
            avatarUrl = "Alice Avatar url",
        )
        val room = aMatrixRoom(
            userDisplayNameResult = { Result.failure(Throwable()) },
            userAvatarUrlResult = { Result.failure(Throwable()) },
            getUpdatedMemberResult = { Result.failure(AN_EXCEPTION) },
        ).apply {
            givenRoomMembersState(MatrixRoomMembersState.Ready(persistentListOf(roomMember)))
        }

        val presenter = createRoomMemberDetailsPresenter(
            room = room,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.userName).isEqualTo("Alice")
            assertThat(initialState.avatarUrl).isEqualTo("Alice Avatar url")
        }
    }

    @Test
    fun `present - will fallback to original data if the updated data is null`() = runTest {
        val roomMember = aRoomMember(displayName = "Alice")
        val room = aMatrixRoom(
            userDisplayNameResult = { Result.success(null) },
            userAvatarUrlResult = { Result.success(null) },
            getUpdatedMemberResult = { Result.success(roomMember) }
        ).apply {
            givenRoomMembersState(MatrixRoomMembersState.Ready(persistentListOf(roomMember)))
        }
        val presenter = createRoomMemberDetailsPresenter(
            room = room,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.userName).isEqualTo("Alice")
            assertThat(initialState.avatarUrl).isEqualTo("Profile avatar url")
        }
    }

    @Test
    fun `present - will fallback to user profile if user is not a member of the room`() = runTest {
        val room = aMatrixRoom(
            userDisplayNameResult = { Result.failure(Exception("Not a member!")) },
            userAvatarUrlResult = { Result.failure(Exception("Not a member!")) },
            getUpdatedMemberResult = { Result.failure(AN_EXCEPTION) },
        )
        val presenter = createRoomMemberDetailsPresenter(
            room = room,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.userName).isEqualTo("Profile user name")
            assertThat(initialState.avatarUrl).isEqualTo("Profile avatar url")
        }
    }

    @Test
    fun `present - null cases`() = runTest {
        val roomMember = aRoomMember(
            displayName = null,
            avatarUrl = null,
        )
        val room = aMatrixRoom(
            userDisplayNameResult = { Result.success(null) },
            userAvatarUrlResult = { Result.success(null) },
            getUpdatedMemberResult = { Result.success(roomMember) },
        )
        val presenter = createRoomMemberDetailsPresenter(
            room = room,
            userProfilePresenterFactory = {
                Presenter {
                    aUserProfileState(
                        userName = null,
                        avatarUrl = null,
                    )
                }
            },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.userName).isNull()
            assertThat(initialState.avatarUrl).isNull()
        }
    }

    private fun createRoomMemberDetailsPresenter(
        room: MatrixRoom,
        userProfilePresenterFactory: UserProfilePresenterFactory = UserProfilePresenterFactory {
            Presenter {
                aUserProfileState(
                    userName = "Profile user name",
                    avatarUrl = "Profile avatar url",
                )
            }
        },
    ): RoomMemberDetailsPresenter {
        return RoomMemberDetailsPresenter(
            roomMemberId = UserId("@alice:server.org"),
            room = room,
            userProfilePresenterFactory = userProfilePresenterFactory
        )
    }
}
