/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.invite

import app.cash.molecule.RecompositionMode
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
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import io.element.android.libraries.usersearch.api.UserSearchResult
import io.element.android.libraries.usersearch.api.UserSearchResultState
import io.element.android.libraries.usersearch.test.FakeUserRepository
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

internal class RoomInviteMembersPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()
    val buildMeta = aBuildMeta()

    @Test
    fun `present - initial state has no results and no search`() = runTest {
        val presenter = RoomInviteMembersPresenter(
            buildMeta = buildMeta,
            userRepository = FakeUserRepository(),
            roomMemberListDataSource = createDataSource(FakeMatrixRoom()),
            coroutineDispatchers = testCoroutineDispatchers()
        )

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            assertThat(initialState.searchResults).isInstanceOf(SearchBarResultState.Initial::class.java)
            assertThat(initialState.isSearchActive).isFalse()
            assertThat(initialState.canInvite).isFalse()
            assertThat(initialState.searchQuery).isEmpty()

            skipItems(1)
        }
    }

    @Test
    fun `present - updates search active state`() = runTest {
        val presenter = RoomInviteMembersPresenter(
            buildMeta = buildMeta,
            userRepository = FakeUserRepository(),
            roomMemberListDataSource = createDataSource(FakeMatrixRoom()),
            coroutineDispatchers = testCoroutineDispatchers()
        )

        moleculeFlow(RecompositionMode.Immediate) {
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
    fun `present - performs search and handles empty result list`() = runTest {
        val repository = FakeUserRepository()
        val presenter = RoomInviteMembersPresenter(
            buildMeta = buildMeta,
            userRepository = repository,
            roomMemberListDataSource = createDataSource(FakeMatrixRoom()),
            coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true)
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomInviteMembersEvents.UpdateSearchQuery("some query"))
            assertThat(repository.providedQuery).isEqualTo("some query")
            repository.emitState(UserSearchResultState(results = emptyList(), isSearching = true))
            consumeItemsUntilPredicate { it.showSearchLoader }.last().also { state ->
                assertThat(state.searchResults).isInstanceOf(SearchBarResultState.Initial::class.java)
                assertThat(state.showSearchLoader).isTrue()
            }
            repository.emitState(results = emptyList(), isSearching = false)
            consumeItemsUntilPredicate { !it.showSearchLoader }.last().also { state ->
                assertThat(state.searchResults).isInstanceOf(SearchBarResultState.NoResultsFound::class.java)
                assertThat(state.showSearchLoader).isFalse()
            }
        }
    }

    @Test
    fun `present - performs search and handles user results`() = runTest {
        val repository = FakeUserRepository()
        val presenter = RoomInviteMembersPresenter(
            buildMeta = buildMeta,
            userRepository = repository,
            roomMemberListDataSource = createDataSource(FakeMatrixRoom()),
            coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true)
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)

            initialState.eventSink(RoomInviteMembersEvents.UpdateSearchQuery("some query"))
            skipItems(1)

            assertThat(repository.providedQuery).isEqualTo("some query")
            repository.emitStateWithUsers(users = aMatrixUserList())
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
            buildMeta = buildMeta,
            userRepository = repository,
            roomMemberListDataSource = createDataSource(
                matrixRoom = FakeMatrixRoom().apply {
                    givenRoomMembersState(
                        MatrixRoomMembersState.Ready(
                            persistentListOf(
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
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)

            initialState.eventSink(RoomInviteMembersEvents.UpdateSearchQuery("some query"))
            skipItems(1)

            assertThat(repository.providedQuery).isEqualTo("some query")
            repository.emitStateWithUsers(users = aMatrixUserList())
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
            buildMeta = buildMeta,
            userRepository = repository,
            roomMemberListDataSource = createDataSource(FakeMatrixRoom().apply {
                givenRoomMembersState(
                    MatrixRoomMembersState.Ready(
                        persistentListOf(
                            aRoomMember(userId = joinedUser.userId, membership = RoomMembershipState.JOIN),
                            aRoomMember(userId = invitedUser.userId, membership = RoomMembershipState.INVITE),
                        )
                    )
                )
            }),
            coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true)
        )

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)

            initialState.eventSink(RoomInviteMembersEvents.UpdateSearchQuery("some query"))
            skipItems(1)

            assertThat(repository.providedQuery).isEqualTo("some query")

            val unresolvedUser = UserSearchResult(aMatrixUser(id = A_USER_ID.value), isUnresolved = true)
            repository.emitState(listOf(unresolvedUser) + aMatrixUserList().map { UserSearchResult(it) })
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
            buildMeta = buildMeta,
            userRepository = repository,
            roomMemberListDataSource = createDataSource(),
            coroutineDispatchers = testCoroutineDispatchers()
        )

        moleculeFlow(RecompositionMode.Immediate) {
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
            buildMeta = buildMeta,
            userRepository = repository,
            roomMemberListDataSource = createDataSource(FakeMatrixRoom()),
            coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true)
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)

            val selectedUser = aMatrixUser()

            initialState.eventSink(RoomInviteMembersEvents.ToggleUser(selectedUser))

            initialState.eventSink(RoomInviteMembersEvents.UpdateSearchQuery("some query"))
            skipItems(1)

            assertThat(repository.providedQuery).isEqualTo("some query")
            repository.emitStateWithUsers(users = aMatrixUserList() + selectedUser)
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
            buildMeta = buildMeta,
            userRepository = repository,
            roomMemberListDataSource = createDataSource(FakeMatrixRoom()),
            coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true)
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)

            val selectedUser = aMatrixUser()

            // Given a query is made
            initialState.eventSink(RoomInviteMembersEvents.UpdateSearchQuery("some query"))
            skipItems(1)

            assertThat(repository.providedQuery).isEqualTo("some query")
            repository.emitStateWithUsers(users = aMatrixUserList() + selectedUser)
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

    private suspend fun FakeUserRepository.emitStateWithUsers(
        users: List<MatrixUser>,
        isSearching: Boolean = false
    ) {
        emitState(
            results = users.map { UserSearchResult(it) },
            isSearching = isSearching,
        )
    }

    private suspend fun FakeUserRepository.emitState(
        results: List<UserSearchResult>,
        isSearching: Boolean = false
    ) {
        val state = UserSearchResultState(
            results = results,
            isSearching = isSearching
        )
        emitState(state)
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
