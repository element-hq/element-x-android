/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rolesandpermissions.impl.roles

import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.RoomModeration
import io.element.android.features.rolesandpermissions.impl.RoomMemberListDataSource
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembersState
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPowerLevels
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID_3
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.matrix.test.room.defaultRoomPowerLevelValues
import io.element.android.libraries.previewutils.room.aRoomMemberList
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.test
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ChangeRolesPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createChangeRolesPresenter()
        presenter.test {
            with(awaitItem()) {
                assertThat(role).isEqualTo(RoomMember.Role.Admin)
                assertThat(query).isNull()
                assertThat(isSearchActive).isFalse()
                assertThat(searchResults).isInstanceOf(SearchBarResultState.Initial::class.java)
                assertThat(selectedUsers).isEmpty()
                assertThat(hasPendingChanges).isFalse()
                assertThat(savingState).isEqualTo(AsyncAction.Uninitialized)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - initial results are loaded automatically`() = runTest {
        val room = FakeJoinedRoom().apply {
            givenRoomMembersState(RoomMembersState.Ready(aRoomMemberList()))
        }
        val presenter = createChangeRolesPresenter(room = room)
        presenter.test {
            skipItems(1)
            assertThat(awaitItem().searchResults).isInstanceOf(SearchBarResultState.Results::class.java)
        }
    }

    @Test
    fun `present - canChangeRole of users with lower power level unless they are owners - privilegedCreatorRole is true`() = runTest {
        val creatorUserId = UserId("@creator:matrix.org")
        val superAdminUserId = UserId("@super_admin:matrix.org")

        val room = FakeJoinedRoom().apply {
            // User is a creator, so they can change roles of other members. So is `creatorUserId`.
            givenRoomInfo(
                aRoomInfo(
                    privilegedCreatorRole = true,
                    roomCreators = listOf(sessionId, creatorUserId),
                    roomPowerLevels = RoomPowerLevels(
                        defaultRoomPowerLevelValues(),
                        users = persistentMapOf(
                            // bob is Admin
                            A_USER_ID_2 to RoomMember.Role.Admin.powerLevel,
                            // carol is Moderator
                            A_USER_ID_3 to RoomMember.Role.Moderator.powerLevel,
                            // super_admin is Owner - Superadmin
                            superAdminUserId to RoomMember.Role.Owner(isCreator = false).powerLevel,
                        )
                    )
                )
            )

            val roomMemberList = aRoomMemberList() + listOf(
                // Owner - superadmin
                aRoomMember(userId = superAdminUserId, role = RoomMember.Role.Owner(isCreator = false)),
                // Owner - creator
                aRoomMember(userId = creatorUserId, role = RoomMember.Role.Owner(isCreator = true))
            )
            givenRoomMembersState(RoomMembersState.Ready(roomMemberList.toImmutableList()))
        }
        val presenter = createChangeRolesPresenter(room = room)
        presenter.test {
            skipItems(1)
            awaitItem().run {
                assertThat(canChangeMemberRole(A_USER_ID_2)).isTrue() // Admin
                assertThat(canChangeMemberRole(A_USER_ID_3)).isTrue() // Moderator
                assertThat(canChangeMemberRole(superAdminUserId)).isTrue() // Super admin
                assertThat(canChangeMemberRole(creatorUserId)).isFalse() // Owner
            }
        }
    }

    @Test
    fun `present - canChangeRole of users with lower power level unless they are owners - privilegedCreatorRole is false`() = runTest {
        val creatorUserId = UserId("@creator:matrix.org")
        val superAdminUserId = UserId("@super_admin:matrix.org")

        val room = FakeJoinedRoom().apply {
            // User is a creator, so they can change roles of other members. So is `creatorUserId`.
            givenRoomInfo(
                aRoomInfo(
                    privilegedCreatorRole = false,
                    roomCreators = listOf(sessionId, creatorUserId),
                    roomPowerLevels = RoomPowerLevels(
                        defaultRoomPowerLevelValues(),
                        users = persistentMapOf(
                            // Creator is an admin
                            sessionId to RoomMember.Role.Admin.powerLevel,
                            creatorUserId to RoomMember.Role.Admin.powerLevel,
                            // bob is Admin
                            A_USER_ID_2 to RoomMember.Role.Admin.powerLevel,
                            // carol is Moderator
                            A_USER_ID_3 to RoomMember.Role.Moderator.powerLevel,
                            // super_admin is Owner - Superadmin
                            superAdminUserId to RoomMember.Role.Owner(isCreator = false).powerLevel,
                        )
                    )
                )
            )

            val roomMemberList = aRoomMemberList() + listOf(
                // Owner - superadmin
                aRoomMember(userId = superAdminUserId, role = RoomMember.Role.Owner(isCreator = false)),
                // Owner - creator
                aRoomMember(userId = creatorUserId, role = RoomMember.Role.Owner(isCreator = true))
            )
            givenRoomMembersState(RoomMembersState.Ready(roomMemberList.toImmutableList()))
        }
        val presenter = createChangeRolesPresenter(room = room)
        presenter.test {
            skipItems(1)
            awaitItem().run {
                assertThat(canChangeMemberRole(A_USER_ID_2)).isFalse() // Creator cannot update Admin in this case
                assertThat(canChangeMemberRole(A_USER_ID_3)).isTrue() // Moderator
                assertThat(canChangeMemberRole(creatorUserId)).isFalse() // Owner
            }
        }
    }

    @Test
    fun `present - when modifying admins, creators are displayed too`() = runTest {
        val room = FakeJoinedRoom().apply {
            val creatorUserId = UserId("@creator:matrix.org")
            val memberList = aRoomMemberList()
                .plus(aRoomMember(displayName = "CREATOR", role = RoomMember.Role.Owner(isCreator = true), userId = creatorUserId))
                .toImmutableList()
            givenRoomInfo(aRoomInfo(roomCreators = listOf(creatorUserId)))
            givenRoomMembersState(RoomMembersState.Ready(memberList))
        }
        val presenter = createChangeRolesPresenter(room = room)
        presenter.test {
            skipItems(2)
            awaitItem().searchResults.run {
                assertThat(this).isInstanceOf(SearchBarResultState.Results::class.java)
                val results = (this as SearchBarResultState.Results).results
                assertThat(results.admins).isNotEmpty()
                assertThat(results.owners).isNotEmpty()
                assertThat(results.owners.last().role).isEqualTo(RoomMember.Role.Owner(isCreator = true))
            }
        }
    }

    @Test
    fun `present - ToggleSearchActive changes the value`() = runTest {
        val room = FakeJoinedRoom().apply {
            givenRoomMembersState(RoomMembersState.Ready(aRoomMemberList()))
        }
        val presenter = createChangeRolesPresenter(room = room)
        presenter.test {
            val initialState = awaitItem()

            initialState.eventSink(ChangeRolesEvent.ToggleSearchActive)
            assertThat(awaitItem().isSearchActive).isTrue()

            initialState.eventSink(ChangeRolesEvent.ToggleSearchActive)
            assertThat(awaitItem().isSearchActive).isFalse()
        }
    }

    @Test
    fun `present - QueryChanged produces new results`() = runTest {
        val room = FakeJoinedRoom().apply {
            givenRoomMembersState(RoomMembersState.Ready(aRoomMemberList()))
        }
        val presenter = createChangeRolesPresenter(room = room)
        presenter.test {
            val initialState = awaitItem()
            val initialResults = (awaitItem().searchResults as? SearchBarResultState.Results)?.results
            assertThat(initialResults?.members).hasSize(8)
            assertThat(initialResults?.moderators).hasSize(1)
            assertThat(initialResults?.admins).hasSize(1)

            initialState.eventSink(ChangeRolesEvent.QueryChanged("Alice"))
            skipItems(1)

            val searchResults = (awaitItem().searchResults as? SearchBarResultState.Results)?.results
            assertThat(searchResults?.admins).hasSize(1)
            assertThat(searchResults?.moderators).isEmpty()
            assertThat(searchResults?.members).isEmpty()
            assertThat(searchResults?.admins?.firstOrNull()?.userId).isEqualTo(A_USER_ID)
        }
    }

    @Test
    fun `present - changes in the room members produce new results`() = runTest {
        val room = FakeJoinedRoom().apply {
            givenRoomMembersState(RoomMembersState.Ready(aRoomMemberList()))
        }
        val presenter = createChangeRolesPresenter(room = room)
        presenter.test {
            skipItems(1)
            val initialResults = (awaitItem().searchResults as? SearchBarResultState.Results)?.results
            assertThat(initialResults?.members).hasSize(8)
            assertThat(initialResults?.moderators).hasSize(1)
            assertThat(initialResults?.admins).hasSize(1)

            room.givenRoomMembersState(RoomMembersState.Ready(aRoomMemberList().take(1).toImmutableList()))
            skipItems(1)

            val searchResults = (awaitItem().searchResults as? SearchBarResultState.Results)?.results
            assertThat(searchResults?.admins).hasSize(1)
            assertThat(searchResults?.moderators).isEmpty()
            assertThat(searchResults?.members).isEmpty()
            assertThat(searchResults?.admins?.firstOrNull()?.userId).isEqualTo(A_USER_ID)
        }
    }

    @Test
    fun `present - UserSelectionToggle adds and removes users from the selected user list`() = runTest {
        val room = FakeJoinedRoom().apply {
            givenRoomMembersState(RoomMembersState.Ready(aRoomMemberList()))
            givenRoomInfo(aRoomInfo(roomPowerLevels = roomPowerLevelsWithRole(RoomMember.Role.Admin)))
        }
        val presenter = createChangeRolesPresenter(room = room)
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.selectedUsers).hasSize(1)

            initialState.eventSink(ChangeRolesEvent.UserSelectionToggled(MatrixUser(A_USER_ID_2)))
            assertThat(awaitItem().selectedUsers).hasSize(2)

            initialState.eventSink(ChangeRolesEvent.UserSelectionToggled(MatrixUser(A_USER_ID_2)))
            assertThat(awaitItem().selectedUsers).hasSize(1)
        }
    }

    @Test
    fun `present - hasPendingChanges is true when the initial selected users don't match the new ones`() = runTest {
        val room = FakeJoinedRoom().apply {
            givenRoomMembersState(RoomMembersState.Ready(aRoomMemberList()))
            givenRoomInfo(aRoomInfo(roomPowerLevels = roomPowerLevelsWithRole(RoomMember.Role.Admin)))
        }
        val presenter = createChangeRolesPresenter(room = room)
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.hasPendingChanges).isFalse()
            assertThat(initialState.selectedUsers).hasSize(1)

            initialState.eventSink(ChangeRolesEvent.UserSelectionToggled(MatrixUser(A_USER_ID_2)))
            with(awaitItem()) {
                assertThat(selectedUsers).hasSize(2)
                assertThat(hasPendingChanges).isTrue()
            }

            initialState.eventSink(ChangeRolesEvent.UserSelectionToggled(MatrixUser(A_USER_ID_2)))
            with(awaitItem()) {
                assertThat(selectedUsers).hasSize(1)
                assertThat(hasPendingChanges).isFalse()
            }
        }
    }

    @Test
    fun `present - Exit will display success false if no pending changes`() = runTest {
        val room = FakeJoinedRoom().apply {
            givenRoomMembersState(RoomMembersState.Ready(aRoomMemberList()))
            givenRoomInfo(aRoomInfo(roomPowerLevels = roomPowerLevelsWithRole(RoomMember.Role.Admin)))
        }
        val presenter = createChangeRolesPresenter(room = room)
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.hasPendingChanges).isFalse()
            assertThat(initialState.savingState).isEqualTo(AsyncAction.Uninitialized)

            initialState.eventSink(ChangeRolesEvent.Exit)
            assertThat(awaitItem().savingState).isEqualTo(AsyncAction.Success(false))
        }
    }

    @Test
    fun `present - CloseDialog will remove exit confirmation`() = runTest {
        val room = FakeJoinedRoom().apply {
            givenRoomMembersState(RoomMembersState.Ready(aRoomMemberList()))
            givenRoomInfo(aRoomInfo(roomPowerLevels = roomPowerLevelsWithRole(RoomMember.Role.Admin)))
        }
        val presenter = createChangeRolesPresenter(room = room)
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.hasPendingChanges).isFalse()
            assertThat(initialState.savingState).isEqualTo(AsyncAction.Uninitialized)

            initialState.eventSink(ChangeRolesEvent.UserSelectionToggled(MatrixUser(A_USER_ID_2)))

            awaitItem().eventSink(ChangeRolesEvent.Exit)
            val confirmingState = awaitItem()
            assertThat(confirmingState.savingState).isEqualTo(AsyncAction.ConfirmingCancellation)

            confirmingState.eventSink(ChangeRolesEvent.CloseDialog)
            assertThat(awaitItem().savingState).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `present - Exit will display a confirmation dialog if there are pending changes, calling it again will actually exit`() = runTest {
        val room = FakeJoinedRoom().apply {
            givenRoomMembersState(RoomMembersState.Ready(aRoomMemberList()))
            givenRoomInfo(aRoomInfo(roomPowerLevels = roomPowerLevelsWithRole(RoomMember.Role.Admin)))
        }
        val presenter = createChangeRolesPresenter(room = room)
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.hasPendingChanges).isFalse()
            assertThat(initialState.savingState).isEqualTo(AsyncAction.Uninitialized)

            initialState.eventSink(ChangeRolesEvent.UserSelectionToggled(MatrixUser(A_USER_ID_2)))
            val updatedState = awaitItem()
            assertThat(updatedState.hasPendingChanges).isTrue()
            skipItems(1)

            updatedState.eventSink(ChangeRolesEvent.Exit)
            assertThat(awaitItem().savingState).isEqualTo(AsyncAction.ConfirmingCancellation)

            updatedState.eventSink(ChangeRolesEvent.Exit)
            assertThat(awaitItem().savingState).isEqualTo(AsyncAction.Success(false))
        }
    }

    @Test
    fun `present - Save will display a confirmation when adding admins`() = runTest {
        val room = FakeJoinedRoom(
            updateUserRoleResult = { Result.success(Unit) },
            baseRoom = FakeBaseRoom(updateMembersResult = { Result.success(Unit) }),
        ).apply {
            givenRoomMembersState(RoomMembersState.Ready(aRoomMemberList()))
            givenRoomInfo(aRoomInfo(roomPowerLevels = roomPowerLevelsWithRole(RoomMember.Role.Admin)))
        }
        val presenter = createChangeRolesPresenter(role = RoomMember.Role.Admin, room = room)
        presenter.test {
            skipItems(2)
            val initialState = awaitItem()
            assertThat(initialState.selectedUsers).hasSize(1)
            initialState.eventSink(ChangeRolesEvent.UserSelectionToggled(MatrixUser(A_USER_ID_2)))
            awaitItem().eventSink(ChangeRolesEvent.Save)
            val confirmingState = awaitItem()
            assertThat(confirmingState.savingState).isEqualTo(ConfirmingModifyingAdmins)
            confirmingState.eventSink(ChangeRolesEvent.Save)
            assertThat(awaitItem().savingState).isInstanceOf(AsyncAction.Loading::class.java)
            assertThat(awaitItem().savingState).isEqualTo(AsyncAction.Success(true))
        }
    }

    @Test
    fun `present - CloseDialog will remove the confirmation dialog`() = runTest {
        val room = FakeJoinedRoom().apply {
            givenRoomMembersState(RoomMembersState.Ready(aRoomMemberList()))
            givenRoomInfo(aRoomInfo(roomPowerLevels = roomPowerLevelsWithRole(RoomMember.Role.Admin)))
        }
        val presenter = createChangeRolesPresenter(role = RoomMember.Role.Admin, room = room)
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.selectedUsers).hasSize(1)

            initialState.eventSink(ChangeRolesEvent.UserSelectionToggled(MatrixUser(A_USER_ID_2)))

            awaitItem().eventSink(ChangeRolesEvent.Save)
            val confirmingState = awaitItem()
            assertThat(confirmingState.savingState).isEqualTo(ConfirmingModifyingAdmins)

            confirmingState.eventSink(ChangeRolesEvent.CloseDialog)
            assertThat(awaitItem().savingState).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `present - Save will just save the data for moderators`() = runTest {
        val analyticsService = FakeAnalyticsService()
        val room = FakeJoinedRoom(
            updateUserRoleResult = { Result.success(Unit) },
            baseRoom = FakeBaseRoom(updateMembersResult = { Result.success(Unit) }),
        ).apply {
            givenRoomMembersState(RoomMembersState.Ready(aRoomMemberList()))
            givenRoomInfo(aRoomInfo(roomPowerLevels = roomPowerLevelsWithRole(RoomMember.Role.Moderator)))
        }
        val presenter = createChangeRolesPresenter(
            role = RoomMember.Role.Moderator,
            room = room,
            analyticsService = analyticsService
        )
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.selectedUsers).isEmpty()
            initialState.eventSink(ChangeRolesEvent.UserSelectionToggled(MatrixUser(A_USER_ID_2)))
            awaitItem().also {
                assertThat(it.selectedUsers).hasSize(1)
                it.eventSink(ChangeRolesEvent.Save)
            }
            assertThat(awaitItem().savingState).isInstanceOf(AsyncAction.Loading::class.java)
            assertThat(awaitItem().savingState).isEqualTo(AsyncAction.Success(true))
            assertThat(analyticsService.capturedEvents.last()).isEqualTo(RoomModeration(RoomModeration.Action.ChangeMemberRole, RoomModeration.Role.Moderator))
        }
    }

    @Test
    fun `present - Save will ask for confirmation before assigning new owners`() = runTest {
        val analyticsService = FakeAnalyticsService()
        val room = FakeJoinedRoom(
            updateUserRoleResult = { Result.success(Unit) },
            baseRoom = FakeBaseRoom(updateMembersResult = { Result.success(Unit) }),
        ).apply {
            givenRoomMembersState(RoomMembersState.Ready(aRoomMemberList()))
            givenRoomInfo(
                aRoomInfo(
                    roomCreators = listOf(sessionId),
                    roomPowerLevels = roomPowerLevelsWithRoles(
                        A_USER_ID to RoomMember.Role.Owner(isCreator = false),
                        A_USER_ID_2 to RoomMember.Role.Admin,
                    )
                )
            )
        }
        val presenter = createChangeRolesPresenter(
            role = RoomMember.Role.Owner(isCreator = false),
            room = room,
            analyticsService = analyticsService
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.selectedUsers).isEmpty()
            initialState.eventSink(ChangeRolesEvent.UserSelectionToggled(MatrixUser(A_USER_ID_2)))
            awaitItem().also {
                assertThat(it.selectedUsers).hasSize(1)
                it.eventSink(ChangeRolesEvent.Save)
            }
            assertThat(awaitItem().savingState.isConfirming()).isTrue()
        }
    }

    @Test
    fun `present - Save will just save the changes if the current user is a room creator and the selected users are not`() = runTest {
        val analyticsService = FakeAnalyticsService()
        val room = FakeJoinedRoom(
            updateUserRoleResult = { Result.success(Unit) },
            baseRoom = FakeBaseRoom(updateMembersResult = { Result.success(Unit) }),
        ).apply {
            givenRoomMembersState(RoomMembersState.Ready(aRoomMemberList()))
            givenRoomInfo(
                aRoomInfo(
                    roomCreators = listOf(sessionId),
                    roomPowerLevels = roomPowerLevelsWithRole(role = RoomMember.Role.Admin, userId = A_USER_ID_2)
                )
            )
        }
        val presenter = createChangeRolesPresenter(
            role = RoomMember.Role.Admin,
            room = room,
            analyticsService = analyticsService
        )
        presenter.test {
            skipItems(2)
            val initialState = awaitItem()
            assertThat(initialState.selectedUsers).hasSize(2)
            initialState.eventSink(ChangeRolesEvent.UserSelectionToggled(MatrixUser(A_USER_ID_2)))
            awaitItem().also {
                assertThat(it.selectedUsers).hasSize(1)
                it.eventSink(ChangeRolesEvent.Save)
            }
            val loadingState = awaitItem()
            assertThat(loadingState.savingState).isInstanceOf(AsyncAction.Loading::class.java)
            assertThat(awaitItem().savingState).isEqualTo(AsyncAction.Success(true))
            assertThat(analyticsService.capturedEvents.last()).isEqualTo(RoomModeration(RoomModeration.Action.ChangeMemberRole, RoomModeration.Role.User))
        }
    }

    @Test
    fun `present - Save can handle failures and CloseDialog clears them`() = runTest {
        val room = FakeJoinedRoom(
            updateUserRoleResult = { Result.failure(IllegalStateException("Failed")) }
        ).apply {
            givenRoomMembersState(RoomMembersState.Ready(aRoomMemberList()))
            givenRoomInfo(aRoomInfo(roomPowerLevels = roomPowerLevelsWithRole(role = RoomMember.Role.Moderator, userId = A_USER_ID)))
        }
        val presenter = createChangeRolesPresenter(role = RoomMember.Role.Moderator, room = room)
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.selectedUsers).isEmpty()
            initialState.eventSink(ChangeRolesEvent.UserSelectionToggled(MatrixUser(A_USER_ID_2)))
            awaitItem().also {
                assertThat(it.selectedUsers).hasSize(1)
                it.eventSink(ChangeRolesEvent.Save)
            }
            val loadingState = awaitItem()
            assertThat(loadingState.savingState).isInstanceOf(AsyncAction.Loading::class.java)
            val failedState = awaitItem()
            assertThat(failedState.savingState).isInstanceOf(AsyncAction.Failure::class.java)
            failedState.eventSink(ChangeRolesEvent.CloseDialog)
            assertThat(awaitItem().savingState).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `test analytics mapping`() = runTest {
        val presenter = createChangeRolesPresenter()
        with(presenter) {
            assertThat(RoomMember.Role.User.toAnalyticsMemberRole()).isEqualTo(RoomModeration.Role.User)
            assertThat(RoomMember.Role.Moderator.toAnalyticsMemberRole()).isEqualTo(RoomModeration.Role.Moderator)
            assertThat(RoomMember.Role.Admin.toAnalyticsMemberRole()).isEqualTo(RoomModeration.Role.Administrator)
            assertThat(RoomMember.Role.Owner(isCreator = false).toAnalyticsMemberRole()).isEqualTo(RoomModeration.Role.Administrator)
            assertThat(RoomMember.Role.Owner(isCreator = true).toAnalyticsMemberRole()).isEqualTo(RoomModeration.Role.Administrator)
        }
    }

    private fun roomPowerLevelsWithRole(
        role: RoomMember.Role,
        userId: UserId = A_USER_ID,
    ): RoomPowerLevels {
        return RoomPowerLevels(
            values = defaultRoomPowerLevelValues(),
            users = persistentMapOf(userId to role.powerLevel)
        )
    }

    private fun roomPowerLevelsWithRoles(vararg pairs: Pair<UserId, RoomMember.Role>): RoomPowerLevels {
        return RoomPowerLevels(
            values = defaultRoomPowerLevelValues(),
            users = pairs.associate { (userId, role) -> userId to role.powerLevel }.toImmutableMap()
        )
    }
}

internal fun TestScope.createChangeRolesPresenter(
    role: RoomMember.Role = RoomMember.Role.Admin,
    room: FakeJoinedRoom = FakeJoinedRoom(baseRoom = FakeBaseRoom(updateMembersResult = {})),
    dispatchers: CoroutineDispatchers = testCoroutineDispatchers(),
    analyticsService: FakeAnalyticsService = FakeAnalyticsService(),
): ChangeRolesPresenter {
    return ChangeRolesPresenter(
        role = role,
        room = room,
        dataSource = RoomMemberListDataSource(room, dispatchers),
        analyticsService = analyticsService,
        roomCoroutineScope = this,
    )
}
