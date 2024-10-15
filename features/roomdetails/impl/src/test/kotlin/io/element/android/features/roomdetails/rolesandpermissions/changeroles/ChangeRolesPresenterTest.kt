/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.rolesandpermissions.changeroles

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.RoomModeration
import io.element.android.features.roomdetails.impl.members.aRoomMemberList
import io.element.android.features.roomdetails.impl.rolesandpermissions.changeroles.ChangeRolesEvent
import io.element.android.features.roomdetails.impl.rolesandpermissions.changeroles.ChangeRolesPresenter
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ChangeRolesPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createChangeRolesPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            with(awaitItem()) {
                assertThat(role).isEqualTo(RoomMember.Role.ADMIN)
                assertThat(query).isNull()
                assertThat(isSearchActive).isFalse()
                assertThat(searchResults).isInstanceOf(SearchBarResultState.Initial::class.java)
                assertThat(selectedUsers).isEmpty()
                assertThat(hasPendingChanges).isFalse()
                assertThat(exitState).isEqualTo(AsyncAction.Uninitialized)
                assertThat(savingState).isEqualTo(AsyncAction.Uninitialized)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - initial results are loaded automatically`() = runTest {
        val room = FakeMatrixRoom().apply {
            givenRoomMembersState(MatrixRoomMembersState.Ready(aRoomMemberList()))
        }
        val presenter = createChangeRolesPresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            assertThat(awaitItem().searchResults).isInstanceOf(SearchBarResultState.Results::class.java)
        }
    }

    @Test
    fun `present - ToggleSearchActive changes the value`() = runTest {
        val room = FakeMatrixRoom().apply {
            givenRoomMembersState(MatrixRoomMembersState.Ready(aRoomMemberList()))
        }
        val presenter = createChangeRolesPresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            initialState.eventSink(ChangeRolesEvent.ToggleSearchActive)
            assertThat(awaitItem().isSearchActive).isTrue()

            initialState.eventSink(ChangeRolesEvent.ToggleSearchActive)
            assertThat(awaitItem().isSearchActive).isFalse()
        }
    }

    @Test
    fun `present - QueryChanged produces new results`() = runTest {
        val room = FakeMatrixRoom().apply {
            givenRoomMembersState(MatrixRoomMembersState.Ready(aRoomMemberList()))
        }
        val presenter = createChangeRolesPresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
        val room = FakeMatrixRoom().apply {
            givenRoomMembersState(MatrixRoomMembersState.Ready(aRoomMemberList()))
        }
        val presenter = createChangeRolesPresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialResults = (awaitItem().searchResults as? SearchBarResultState.Results)?.results
            assertThat(initialResults?.members).hasSize(8)
            assertThat(initialResults?.moderators).hasSize(1)
            assertThat(initialResults?.admins).hasSize(1)

            room.givenRoomMembersState(MatrixRoomMembersState.Ready(aRoomMemberList().take(1).toPersistentList()))
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
        val room = FakeMatrixRoom().apply {
            givenRoomMembersState(MatrixRoomMembersState.Ready(aRoomMemberList()))
            givenRoomInfo(aRoomInfo(userPowerLevels = persistentMapOf(A_USER_ID to 100)))
        }
        val presenter = createChangeRolesPresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
        val room = FakeMatrixRoom().apply {
            givenRoomMembersState(MatrixRoomMembersState.Ready(aRoomMemberList()))
            givenRoomInfo(aRoomInfo(userPowerLevels = persistentMapOf(A_USER_ID to 100)))
        }
        val presenter = createChangeRolesPresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
    fun `present - Exit will display success if no pending changes`() = runTest {
        val room = FakeMatrixRoom().apply {
            givenRoomMembersState(MatrixRoomMembersState.Ready(aRoomMemberList()))
            givenRoomInfo(aRoomInfo(userPowerLevels = persistentMapOf(A_USER_ID to 100)))
        }
        val presenter = createChangeRolesPresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.hasPendingChanges).isFalse()
            assertThat(initialState.exitState).isEqualTo(AsyncAction.Uninitialized)

            initialState.eventSink(ChangeRolesEvent.Exit)
            assertThat(awaitItem().exitState).isEqualTo(AsyncAction.Success(Unit))
        }
    }

    @Test
    fun `present - CancelExit will remove exit confirmation`() = runTest {
        val room = FakeMatrixRoom().apply {
            givenRoomMembersState(MatrixRoomMembersState.Ready(aRoomMemberList()))
            givenRoomInfo(aRoomInfo(userPowerLevels = persistentMapOf(A_USER_ID to 100)))
        }
        val presenter = createChangeRolesPresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.hasPendingChanges).isFalse()
            assertThat(initialState.exitState).isEqualTo(AsyncAction.Uninitialized)

            initialState.eventSink(ChangeRolesEvent.UserSelectionToggled(MatrixUser(A_USER_ID_2)))

            awaitItem().eventSink(ChangeRolesEvent.Exit)
            val confirmingState = awaitItem()
            assertThat(confirmingState.exitState).isEqualTo(AsyncAction.ConfirmingNoParams)

            confirmingState.eventSink(ChangeRolesEvent.CancelExit)
            assertThat(awaitItem().exitState).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `present - Exit will display a confirmation dialog if there are pending changes, calling it again will actually exit`() = runTest {
        val room = FakeMatrixRoom().apply {
            givenRoomMembersState(MatrixRoomMembersState.Ready(aRoomMemberList()))
            givenRoomInfo(aRoomInfo(userPowerLevels = persistentMapOf(A_USER_ID to 100)))
        }
        val presenter = createChangeRolesPresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.hasPendingChanges).isFalse()
            assertThat(initialState.exitState).isEqualTo(AsyncAction.Uninitialized)

            initialState.eventSink(ChangeRolesEvent.UserSelectionToggled(MatrixUser(A_USER_ID_2)))
            val updatedState = awaitItem()
            assertThat(updatedState.hasPendingChanges).isTrue()
            skipItems(1)

            updatedState.eventSink(ChangeRolesEvent.Exit)
            assertThat(awaitItem().exitState).isEqualTo(AsyncAction.ConfirmingNoParams)

            updatedState.eventSink(ChangeRolesEvent.Exit)
            assertThat(awaitItem().exitState).isEqualTo(AsyncAction.Success(Unit))
        }
    }

    @Test
    fun `present - Save will display a confirmation when adding admins`() = runTest {
        val room = FakeMatrixRoom(
            updateUserRoleResult = { Result.success(Unit) },
            updateMembersResult = { Result.success(Unit) },
        ).apply {
            givenRoomMembersState(MatrixRoomMembersState.Ready(aRoomMemberList()))
            givenRoomInfo(aRoomInfo(userPowerLevels = persistentMapOf(A_USER_ID to 100)))
        }
        val presenter = createChangeRolesPresenter(role = RoomMember.Role.ADMIN, room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.selectedUsers).hasSize(1)

            initialState.eventSink(ChangeRolesEvent.UserSelectionToggled(MatrixUser(A_USER_ID_2)))
            awaitItem().eventSink(ChangeRolesEvent.Save)
            val confirmingState = awaitItem()
            assertThat(confirmingState.savingState).isEqualTo(AsyncAction.ConfirmingNoParams)

            confirmingState.eventSink(ChangeRolesEvent.Save)
            assertThat(awaitItem().savingState).isEqualTo(AsyncAction.Success(Unit))
        }
    }

    @Test
    fun `present - CancelSave will remove the confirmation dialog`() = runTest {
        val room = FakeMatrixRoom().apply {
            givenRoomMembersState(MatrixRoomMembersState.Ready(aRoomMemberList()))
            givenRoomInfo(aRoomInfo(userPowerLevels = persistentMapOf(A_USER_ID to 100)))
        }
        val presenter = createChangeRolesPresenter(role = RoomMember.Role.ADMIN, room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.selectedUsers).hasSize(1)

            initialState.eventSink(ChangeRolesEvent.UserSelectionToggled(MatrixUser(A_USER_ID_2)))

            awaitItem().eventSink(ChangeRolesEvent.Save)
            val confirmingState = awaitItem()
            assertThat(confirmingState.savingState).isEqualTo(AsyncAction.ConfirmingNoParams)

            confirmingState.eventSink(ChangeRolesEvent.CancelSave)
            assertThat(awaitItem().savingState).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `present - Save will just save the data for moderators`() = runTest {
        val analyticsService = FakeAnalyticsService()
        val room = FakeMatrixRoom(
            updateUserRoleResult = { Result.success(Unit) },
            updateMembersResult = { Result.success(Unit) },
        ).apply {
            givenRoomMembersState(MatrixRoomMembersState.Ready(aRoomMemberList()))
            givenRoomInfo(aRoomInfo(userPowerLevels = persistentMapOf(A_USER_ID to 50)))
        }
        val presenter = createChangeRolesPresenter(
            role = RoomMember.Role.MODERATOR,
            room = room,
            analyticsService = analyticsService
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.selectedUsers).hasSize(1)

            initialState.eventSink(ChangeRolesEvent.UserSelectionToggled(MatrixUser(A_USER_ID_2)))

            awaitItem().eventSink(ChangeRolesEvent.Save)
            assertThat(awaitItem().savingState).isEqualTo(AsyncAction.Success(Unit))
            assertThat(analyticsService.capturedEvents.last()).isEqualTo(RoomModeration(RoomModeration.Action.ChangeMemberRole, RoomModeration.Role.Moderator))
        }
    }

    @Test
    fun `present - Save can handle failures and ClearError clears them`() = runTest {
        val room = FakeMatrixRoom(
            updateUserRoleResult = { Result.failure(IllegalStateException("Failed")) }
        ).apply {
            givenRoomMembersState(MatrixRoomMembersState.Ready(aRoomMemberList()))
            givenRoomInfo(aRoomInfo(userPowerLevels = persistentMapOf(A_USER_ID to 50)))
        }
        val presenter = createChangeRolesPresenter(role = RoomMember.Role.MODERATOR, room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.selectedUsers).hasSize(1)

            initialState.eventSink(ChangeRolesEvent.UserSelectionToggled(MatrixUser(A_USER_ID_2)))

            awaitItem().eventSink(ChangeRolesEvent.Save)
            val failedState = awaitItem()
            assertThat(failedState.savingState).isInstanceOf(AsyncAction.Failure::class.java)

            failedState.eventSink(ChangeRolesEvent.ClearError)
            assertThat(awaitItem().savingState).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    private fun TestScope.createChangeRolesPresenter(
        role: RoomMember.Role = RoomMember.Role.ADMIN,
        room: FakeMatrixRoom = FakeMatrixRoom(),
        dispatchers: CoroutineDispatchers = testCoroutineDispatchers(),
        analyticsService: FakeAnalyticsService = FakeAnalyticsService(),
    ): ChangeRolesPresenter {
        return ChangeRolesPresenter(
            role = role,
            room = room,
            dispatchers = dispatchers,
            analyticsService = analyticsService,
        )
    }
}
