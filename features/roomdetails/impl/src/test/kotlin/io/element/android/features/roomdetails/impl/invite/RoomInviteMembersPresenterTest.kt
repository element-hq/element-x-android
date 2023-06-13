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

package io.element.android.features.roomdetails.impl.invite

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.roomdetails.aMatrixRoom
import io.element.android.features.roomdetails.impl.members.RoomMemberListDataSource
import io.element.android.features.roomdetails.impl.members.aRoomMember
import io.element.android.features.roomdetails.impl.members.aRoomMemberList
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.room.aFakeMatrixRoom
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import io.element.android.libraries.usersearch.api.UserSearchResult
import io.element.android.libraries.usersearch.test.FakeUserRepository
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class RoomInviteMembersPresenterTest {

    @Test
    fun `present - initial state has no results and no search`() = runTest {
        val presenter = RoomInviteMembersPresenter(
            userRepository = FakeUserRepository(),
            roomMemberListDataSource = createDataSource(aFakeMatrixRoom()),
            coroutineDispatchers = testCoroutineDispatchers()
        )

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            assertThat(initialState.searchResults).isInstanceOf(SearchBarResultState.NotSearching::class.java)
            assertThat(initialState.isSearchActive).isFalse()
            assertThat(initialState.canInvite).isFalse()
            assertThat(initialState.searchQuery).isEmpty()

            skipItems(1)
        }
    }

    @Test
    fun `present - updates search active state`() = runTest {
        val presenter = RoomInviteMembersPresenter(
            userRepository = FakeUserRepository(),
            roomMemberListDataSource = createDataSource(aFakeMatrixRoom()),
            coroutineDispatchers = testCoroutineDispatchers()
        )

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)

            initialState.eventSink(RoomInviteMembersEvents.OnSearchActiveChanged(true))

            val resultState = awaitItem()
            assertThat(resultState.isSearchActive).isTrue()
        }
    }

    @Test
    fun `present - performs search and handles no results`() = runTest {
        val repository = FakeUserRepository()
        val presenter = RoomInviteMembersPresenter(
            userRepository = repository,
            roomMemberListDataSource = createDataSource(aFakeMatrixRoom()),
            coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true)
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)

            initialState.eventSink(RoomInviteMembersEvents.UpdateSearchQuery("some query"))
            skipItems(1)

            assertThat(repository.providedQuery).isEqualTo("some query")
            repository.emitResult(emptyList())
            skipItems(1)

            val resultState = awaitItem()
            assertThat(resultState.searchResults).isInstanceOf(SearchBarResultState.NoResults::class.java)
        }
    }

    @Test
    fun `present - performs search and handles user results`() = runTest {
        val repository = FakeUserRepository()
        val presenter = RoomInviteMembersPresenter(
            userRepository = repository,
            roomMemberListDataSource = createDataSource(aFakeMatrixRoom()),
            coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true)
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)

            initialState.eventSink(RoomInviteMembersEvents.UpdateSearchQuery("some query"))
            skipItems(1)

            assertThat(repository.providedQuery).isEqualTo("some query")
            repository.emitResult(aMatrixUserList().map { UserSearchResult(it) })
            skipItems(1)

            val resultState = awaitItem()
            assertThat(resultState.searchResults).isInstanceOf(SearchBarResultState.Results::class.java)

            val expectedUsers = aMatrixUserList()
            val users = resultState.searchResults.users()
            expectedUsers.forEachIndexed { index, matrixUser ->
                assertThat(users[index].matrixUser).isEqualTo(matrixUser)
                assertThat(users[index].isAlreadyInvited).isFalse()
                assertThat(users[index].isAlreadyJoined).isFalse()
                assertThat(users[index].isSelected).isFalse()
            }
        }
    }

    @Test
    fun `present - performs search and handles membership state of existing users`() = runTest {
        val userList = aMatrixUserList()
        val joinedUser = userList[0]
        val invitedUser = userList[1]

        val repository = FakeUserRepository()
        val coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true)
        val presenter = RoomInviteMembersPresenter(
            userRepository = repository,
            roomMemberListDataSource = createDataSource(
                matrixRoom = aFakeMatrixRoom(coroutineDispatchers = coroutineDispatchers).apply {
                    givenRoomMembersState(
                        MatrixRoomMembersState.Ready(
                            listOf(
                                aRoomMember(userId = joinedUser.userId, membership = RoomMembershipState.JOIN),
                                aRoomMember(userId = invitedUser.userId, membership = RoomMembershipState.INVITE),
                            )
                        )
                    )
                },
                coroutineDispatchers = coroutineDispatchers,
            ),
            coroutineDispatchers = coroutineDispatchers
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)

            initialState.eventSink(RoomInviteMembersEvents.UpdateSearchQuery("some query"))
            skipItems(1)

            assertThat(repository.providedQuery).isEqualTo("some query")
            repository.emitResult(aMatrixUserList().map { UserSearchResult(it) })
            skipItems(1)

            val resultState = awaitItem()
            assertThat(resultState.searchResults).isInstanceOf(SearchBarResultState.Results::class.java)

            val users = resultState.searchResults.users()

            // The result that matches a user with JOINED membership is marked as such
            val userWhoShouldBeJoined = users.find { it.matrixUser == joinedUser }
            assertThat(userWhoShouldBeJoined).isNotNull()
            assertThat(userWhoShouldBeJoined?.isAlreadyJoined).isTrue()
            assertThat(userWhoShouldBeJoined?.isAlreadyInvited).isFalse()

            // The result that matches a user with INVITED membership is marked as such
            val userWhoShouldBeInvited = users.find { it.matrixUser == invitedUser }
            assertThat(userWhoShouldBeInvited).isNotNull()
            assertThat(userWhoShouldBeInvited?.isAlreadyJoined).isFalse()
            assertThat(userWhoShouldBeInvited?.isAlreadyInvited).isTrue()

            // All other users are neither joined nor invited
            val otherUsers = users.minus(userWhoShouldBeInvited!!).minus(userWhoShouldBeJoined!!)
            assertThat(otherUsers.none { it.isAlreadyInvited }).isTrue()
            assertThat(otherUsers.none { it.isAlreadyJoined }).isTrue()
        }
    }

    @Test
    fun `present - performs search and handles unresolved results`() = runTest {
        val userList = aMatrixUserList()
        val joinedUser = userList[0]
        val invitedUser = userList[1]

        val repository = FakeUserRepository()
        val presenter = RoomInviteMembersPresenter(
            userRepository = repository,
            roomMemberListDataSource = createDataSource(aFakeMatrixRoom().apply {
                givenRoomMembersState(
                    MatrixRoomMembersState.Ready(
                        listOf(
                            aRoomMember(userId = joinedUser.userId, membership = RoomMembershipState.JOIN),
                            aRoomMember(userId = invitedUser.userId, membership = RoomMembershipState.INVITE),
                        )
                    )
                )
            }),
            coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true)
        )

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)

            initialState.eventSink(RoomInviteMembersEvents.UpdateSearchQuery("some query"))
            skipItems(1)

            assertThat(repository.providedQuery).isEqualTo("some query")

            val unresolvedUser = UserSearchResult(aMatrixUser(id = A_USER_ID.value), isUnresolved = true)
            repository.emitResult(listOf(unresolvedUser) + aMatrixUserList().map { UserSearchResult(it) })
            skipItems(1)

            val resultState = awaitItem()
            assertThat(resultState.searchResults).isInstanceOf(SearchBarResultState.Results::class.java)

            val users = resultState.searchResults.users()

            val userWhoShouldBeUnresolved = users.first()
            assertThat(userWhoShouldBeUnresolved.isUnresolved).isTrue()

            // All other users are neither joined nor invited
            val otherUsers = users.minus(userWhoShouldBeUnresolved)
            assertThat(otherUsers.none { it.isUnresolved }).isTrue()
        }
    }

    @Test
    fun `present - toggle users updates selected user state`() = runTest {
        val repository = FakeUserRepository()
        val presenter = RoomInviteMembersPresenter(
            userRepository = repository,
            roomMemberListDataSource = createDataSource(),
            coroutineDispatchers = testCoroutineDispatchers()
        )

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)

            // When we toggle a user not in the list, they are added
            initialState.eventSink(RoomInviteMembersEvents.ToggleUser(aMatrixUser()))
            assertThat(awaitItem().selectedUsers).containsExactly(aMatrixUser())

            // Toggling a different user also adds them
            initialState.eventSink(RoomInviteMembersEvents.ToggleUser(aMatrixUser(id = A_USER_ID_2.value)))
            assertThat(awaitItem().selectedUsers).containsExactly(aMatrixUser(), aMatrixUser(id = A_USER_ID_2.value))

            // Toggling the first user removes them
            initialState.eventSink(RoomInviteMembersEvents.ToggleUser(aMatrixUser()))
            assertThat(awaitItem().selectedUsers).containsExactly(aMatrixUser(id = A_USER_ID_2.value))
        }
    }

    @Test
    fun `present - selected users appear as such in search results`() = runTest {
        val repository = FakeUserRepository()
        val presenter = RoomInviteMembersPresenter(
            userRepository = repository,
            roomMemberListDataSource = createDataSource(aFakeMatrixRoom()),
            coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true)
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)

            val selectedUser = aMatrixUser()

            initialState.eventSink(RoomInviteMembersEvents.ToggleUser(selectedUser))

            initialState.eventSink(RoomInviteMembersEvents.UpdateSearchQuery("some query"))
            skipItems(1)

            assertThat(repository.providedQuery).isEqualTo("some query")
            repository.emitResult((aMatrixUserList() + selectedUser).map { UserSearchResult(it) })
            skipItems(2)

            val resultState = awaitItem()
            assertThat(resultState.searchResults).isInstanceOf(SearchBarResultState.Results::class.java)

            val users = resultState.searchResults.users()

            // The one user we have previously toggled is marked as selected
            val shouldBeSelectedUser = users.find { it.matrixUser == selectedUser }
            assertThat(shouldBeSelectedUser).isNotNull()
            assertThat(shouldBeSelectedUser?.isSelected).isTrue()

            // And no others are
            val allOtherUsers = users.minus(shouldBeSelectedUser!!)
            assertThat(allOtherUsers.none { it.isSelected }).isTrue()
        }
    }

    @Test
    fun `present - toggling a user updates existing search results`() = runTest {
        val repository = FakeUserRepository()
        val presenter = RoomInviteMembersPresenter(
            userRepository = repository,
            roomMemberListDataSource = createDataSource(aFakeMatrixRoom()),
            coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true)
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)

            val selectedUser = aMatrixUser()

            // Given a query is made
            initialState.eventSink(RoomInviteMembersEvents.UpdateSearchQuery("some query"))
            skipItems(1)

            assertThat(repository.providedQuery).isEqualTo("some query")
            repository.emitResult((aMatrixUserList() + selectedUser).map { UserSearchResult(it) })
            skipItems(2)

            // And then a user is toggled
            initialState.eventSink(RoomInviteMembersEvents.ToggleUser(selectedUser))
            skipItems(1)
            val resultState = awaitItem()

            // The results are updated...
            assertThat(resultState.searchResults).isInstanceOf(SearchBarResultState.Results::class.java)
            val users = resultState.searchResults.users()

            // The one user we have now toggled is marked as selected
            val shouldBeSelectedUser = users.find { it.matrixUser == selectedUser }
            assertThat(shouldBeSelectedUser).isNotNull()
            assertThat(shouldBeSelectedUser?.isSelected).isTrue()

            // And no others are
            val allOtherUsers = users.minus(shouldBeSelectedUser!!)
            assertThat(allOtherUsers.none { it.isSelected }).isTrue()
        }
    }

    private fun TestScope.createDataSource(
        matrixRoom: MatrixRoom = aMatrixRoom().apply {
            givenRoomMembersState(MatrixRoomMembersState.Ready(aRoomMemberList()))
        },
        coroutineDispatchers: CoroutineDispatchers = testCoroutineDispatchers()
    ) = RoomMemberListDataSource(matrixRoom, coroutineDispatchers)

    private fun SearchBarResultState<ImmutableList<InvitableUser>>.users() =
        (this as? SearchBarResultState.Results<ImmutableList<InvitableUser>>)?.results.orEmpty()
}
