/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invitepeople.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.room.RoomMembersState
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.matrix.test.room.aRoomMemberList
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import io.element.android.libraries.usersearch.api.UserRepository
import io.element.android.libraries.usersearch.api.UserSearchResult
import io.element.android.libraries.usersearch.api.UserSearchResultState
import io.element.android.libraries.usersearch.test.FakeUserRepository
import io.element.android.services.apperror.api.AppErrorStateService
import io.element.android.services.apperror.test.FakeAppErrorStateService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

internal class DefaultInvitePeoplePresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state has no results and no search`() = runTest {
        val presenter = createDefaultInvitePeoplePresenter()

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItemAsDefault()

            assertThat(initialState.searchResults).isInstanceOf(SearchBarResultState.Initial::class.java)
            assertThat(initialState.isSearchActive).isFalse()
            assertThat(initialState.canInvite).isFalse()
            assertThat(initialState.searchQuery).isEmpty()

            skipItems(1)
        }
    }

    @Test
    fun `present - updates search active state`() = runTest {
        val presenter = createDefaultInvitePeoplePresenter()

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)

            initialState.eventSink(DefaultInvitePeopleEvents.OnSearchActiveChanged(true))

            val resultState = awaitItem()
            assertThat(resultState.isSearchActive).isTrue()
        }
    }

    @Test
    fun `present - performs search and handles empty result list`() = runTest {
        val repository = FakeUserRepository()
        val presenter = createDefaultInvitePeoplePresenter(
            userRepository = repository,
            coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true)
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(DefaultInvitePeopleEvents.UpdateSearchQuery("some query"))
            assertThat(repository.providedQuery).isEqualTo("some query")
            repository.emitState(UserSearchResultState(results = emptyList(), isSearching = true))
            skipItems(3)
            awaitItemAsDefault().also { state ->
                assertThat(state.searchResults).isInstanceOf(SearchBarResultState.Initial::class.java)
                assertThat(state.showSearchLoader).isTrue()
            }
            repository.emitState(results = emptyList(), isSearching = false)
            awaitItemAsDefault().also { state ->
                assertThat(state.searchResults).isInstanceOf(SearchBarResultState.NoResultsFound::class.java)
                assertThat(state.showSearchLoader).isFalse()
            }
        }
    }

    @Test
    fun `present - performs search and handles user results`() = runTest {
        val repository = FakeUserRepository()
        val presenter = createDefaultInvitePeoplePresenter(
            userRepository = repository,
            coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true)
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)

            initialState.eventSink(DefaultInvitePeopleEvents.UpdateSearchQuery("some query"))
            skipItems(1)

            assertThat(repository.providedQuery).isEqualTo("some query")
            repository.emitStateWithUsers(users = aMatrixUserList())
            skipItems(1)

            val resultState = awaitItemAsDefault()
            assertThat(resultState.searchResults).isInstanceOf(SearchBarResultState.Results::class.java)

            val expectedUsers = aMatrixUserList()
            val users = resultState.searchResults.users()
            expectedUsers.forEachIndexed { index, matrixUser ->
                assertThat(users[index].matrixUser).isEqualTo(matrixUser)
                // All users are joined or invited
                if (users[index].isAlreadyInvited) {
                    assertThat(users[index].isAlreadyJoined).isFalse()
                } else {
                    assertThat(users[index].isAlreadyJoined).isTrue()
                }
                assertThat(users[index].isSelected).isTrue()
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
        val presenter = createDefaultInvitePeoplePresenter(
            userRepository = repository,
            roomMembersState = RoomMembersState.Ready(
                persistentListOf(
                    aRoomMember(
                        userId = joinedUser.userId,
                        membership = RoomMembershipState.JOIN
                    ),
                    aRoomMember(
                        userId = invitedUser.userId,
                        membership = RoomMembershipState.INVITE
                    ),
                )
            ),
            coroutineDispatchers = coroutineDispatchers,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)

            initialState.eventSink(DefaultInvitePeopleEvents.UpdateSearchQuery("some query"))
            skipItems(1)

            assertThat(repository.providedQuery).isEqualTo("some query")
            repository.emitStateWithUsers(users = aMatrixUserList())
            skipItems(1)

            val resultState = awaitItemAsDefault()
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
        val presenter = createDefaultInvitePeoplePresenter(
            userRepository = repository,
            roomMembersState =
                RoomMembersState.Ready(
                    persistentListOf(
                        aRoomMember(
                            userId = joinedUser.userId,
                            membership = RoomMembershipState.JOIN
                        ),
                        aRoomMember(
                            userId = invitedUser.userId,
                            membership = RoomMembershipState.INVITE
                        ),
                    )
                ),
            coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true)
        )

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)

            initialState.eventSink(DefaultInvitePeopleEvents.UpdateSearchQuery("some query"))
            skipItems(1)

            assertThat(repository.providedQuery).isEqualTo("some query")

            val unresolvedUser =
                UserSearchResult(aMatrixUser(id = A_USER_ID.value), isUnresolved = true)
            repository.emitState(listOf(unresolvedUser) + aMatrixUserList().map {
                UserSearchResult(
                    it
                )
            })
            skipItems(1)

            val resultState = awaitItemAsDefault()
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
        val presenter = createDefaultInvitePeoplePresenter(
            userRepository = repository,
            coroutineDispatchers = testCoroutineDispatchers()
        )

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)

            // When we toggle a user not in the list, they are added
            initialState.eventSink(DefaultInvitePeopleEvents.ToggleUser(aMatrixUser()))
            assertThat(awaitItemAsDefault().selectedUsers).containsExactly(aMatrixUser())

            // Toggling a different user also adds them
            initialState.eventSink(DefaultInvitePeopleEvents.ToggleUser(aMatrixUser(id = A_USER_ID_2.value)))
            assertThat(awaitItemAsDefault().selectedUsers).containsExactly(
                aMatrixUser(),
                aMatrixUser(id = A_USER_ID_2.value)
            )

            // Toggling the first user removes them
            initialState.eventSink(DefaultInvitePeopleEvents.ToggleUser(aMatrixUser()))
            assertThat(awaitItemAsDefault().selectedUsers).containsExactly(aMatrixUser(id = A_USER_ID_2.value))
        }
    }

    @Test
    fun `present - selected users appear as such in search results`() = runTest {
        val repository = FakeUserRepository()
        val presenter = createDefaultInvitePeoplePresenter(
            userRepository = repository,
            coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true)
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)

            val selectedUser = aMatrixUser()

            initialState.eventSink(DefaultInvitePeopleEvents.ToggleUser(selectedUser))

            initialState.eventSink(DefaultInvitePeopleEvents.UpdateSearchQuery("some query"))
            skipItems(1)

            assertThat(repository.providedQuery).isEqualTo("some query")
            repository.emitStateWithUsers(users = aMatrixUserList() + selectedUser)
            skipItems(2)

            val resultState = awaitItemAsDefault()
            assertThat(resultState.searchResults).isInstanceOf(SearchBarResultState.Results::class.java)

            val users = resultState.searchResults.users()

            // The one user we have previously toggled is marked as selected
            val shouldBeSelectedUser = users.find { it.matrixUser == selectedUser }
            assertThat(shouldBeSelectedUser).isNotNull()
            assertThat(shouldBeSelectedUser?.isSelected).isTrue()

            // All the others are selected since their membership is joined or invited
            val allOtherUsers = users.minus(shouldBeSelectedUser!!)
            assertThat(allOtherUsers.all { it.isSelected }).isTrue()
        }
    }

    @Test
    fun `present - toggling a user updates existing search results`() = runTest {
        val repository = FakeUserRepository()
        val presenter = createDefaultInvitePeoplePresenter(
            userRepository = repository,
            coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true)
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)

            val selectedUser = aMatrixUser()

            // Given a query is made
            initialState.eventSink(DefaultInvitePeopleEvents.UpdateSearchQuery("some query"))
            skipItems(1)

            assertThat(repository.providedQuery).isEqualTo("some query")
            repository.emitStateWithUsers(users = aMatrixUserList() + selectedUser)
            skipItems(1)
            awaitItemAsDefault().also { state ->
                // selectedUser is not selected
                assertThat(state.searchResults).isInstanceOf(SearchBarResultState.Results::class.java)
                val users = state.searchResults.users()
                val shouldNotBeSelectedUser = users.find { it.matrixUser == selectedUser }
                assertThat(shouldNotBeSelectedUser).isNotNull()
                assertThat(shouldNotBeSelectedUser?.isSelected).isFalse()
            }

            // And then a user is toggled
            initialState.eventSink(DefaultInvitePeopleEvents.ToggleUser(selectedUser))
            skipItems(1)
            val resultState = awaitItemAsDefault()

            // The results are updated...
            assertThat(resultState.searchResults).isInstanceOf(SearchBarResultState.Results::class.java)
            val users = resultState.searchResults.users()

            // The one user we have now toggled is marked as selected
            val shouldBeSelectedUser = users.find { it.matrixUser == selectedUser }
            assertThat(shouldBeSelectedUser).isNotNull()
            assertThat(shouldBeSelectedUser?.isSelected).isTrue()

            // All the others are selected since their membership is joined or invited
            val allOtherUsers = users.minus(shouldBeSelectedUser!!)
            assertThat(allOtherUsers.all { it.isSelected }).isTrue()
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

    private fun SearchBarResultState<ImmutableList<InvitableUser>>.users() =
        (this as? SearchBarResultState.Results<ImmutableList<InvitableUser>>)?.results.orEmpty()
}

private suspend fun <T> ReceiveTurbine<T>.awaitItemAsDefault(): DefaultInvitePeopleState {
    return awaitItem() as DefaultInvitePeopleState
}

fun TestScope.createDefaultInvitePeoplePresenter(
    roomMembersState: RoomMembersState = RoomMembersState.Ready(aRoomMemberList()),
    userRepository: UserRepository = FakeUserRepository(),
    coroutineDispatchers: CoroutineDispatchers = testCoroutineDispatchers(),
    appErrorStateService: AppErrorStateService = FakeAppErrorStateService(),
): DefaultInvitePeoplePresenter {
    return DefaultInvitePeoplePresenter(
        room = FakeJoinedRoom().apply {
            givenRoomMembersState(roomMembersState)
        },
        userRepository = userRepository,
        coroutineDispatchers = coroutineDispatchers,
        coroutineScope = backgroundScope,
        appErrorStateService = appErrorStateService,
    )
}
