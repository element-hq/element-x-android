/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rolesandpermissions.impl.root

import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.RoomModeration
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembersState
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomMemberList
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.test
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RolesAndPermissionPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createRolesAndPermissionsPresenter()
        presenter.test {
            with(awaitItem()) {
                assertThat(adminCount).isNull()
                assertThat(moderatorCount).isNull()
                assertThat(changeOwnRoleAction).isEqualTo(AsyncAction.Uninitialized)
            }
        }
    }

    @Test
    fun `present - ChangeOwnRole presents a confirmation dialog`() = runTest {
        val presenter = createRolesAndPermissionsPresenter()
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(RolesAndPermissionsEvents.ChangeOwnRole)
            assertThat(awaitItem().changeOwnRoleAction).isEqualTo(AsyncAction.ConfirmingNoParams)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `present - DemoteSelfTo changes own role to the specified one`() = runTest(StandardTestDispatcher()) {
        val presenter = createRolesAndPermissionsPresenter(
            dispatchers = testCoroutineDispatchers(),
            room = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(updateMembersResult = {}),
                updateUserRoleResult = { Result.success(Unit) }
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(RolesAndPermissionsEvents.DemoteSelfTo(RoomMember.Role.Moderator))

            runCurrent()
            assertThat(awaitItem().changeOwnRoleAction).isEqualTo(AsyncAction.Loading)

            runCurrent()
            assertThat(awaitItem().changeOwnRoleAction).isEqualTo(AsyncAction.Success(Unit))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `present - DemoteSelfTo can handle failures and clean them`() = runTest(StandardTestDispatcher()) {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(updateMembersResult = {}),
            updateUserRoleResult = { Result.failure(Exception("Failed to update role")) }
        )
        val presenter = createRolesAndPermissionsPresenter(room = room, dispatchers = testCoroutineDispatchers())
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(RolesAndPermissionsEvents.DemoteSelfTo(RoomMember.Role.Moderator))

            runCurrent()
            assertThat(awaitItem().changeOwnRoleAction).isEqualTo(AsyncAction.Loading)

            runCurrent()
            assertThat(awaitItem().changeOwnRoleAction).isInstanceOf(AsyncAction.Failure::class.java)

            initialState.eventSink(RolesAndPermissionsEvents.CancelPendingAction)
            assertThat(awaitItem().changeOwnRoleAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `present - CancelPendingAction dismisses confirmation dialog too`() = runTest {
        val presenter = createRolesAndPermissionsPresenter()
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(RolesAndPermissionsEvents.ChangeOwnRole)
            awaitItem().eventSink(RolesAndPermissionsEvents.CancelPendingAction)

            assertThat(awaitItem().changeOwnRoleAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `present - ResetPermissions needs confirmation, then resets permissions`() = runTest {
        val analyticsService = FakeAnalyticsService()
        val presenter = createRolesAndPermissionsPresenter(
            analyticsService = analyticsService,
            room = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(updateMembersResult = {}),
                resetPowerLevelsResult = { Result.success(Unit) }
            )
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(RolesAndPermissionsEvents.ResetPermissions)
            // Confirmation
            awaitItem().eventSink(RolesAndPermissionsEvents.ResetPermissions)

            assertThat(awaitItem().resetPermissionsAction).isEqualTo(AsyncAction.Loading)
            assertThat(awaitItem().resetPermissionsAction).isEqualTo(AsyncAction.Success(Unit))
            assertThat(analyticsService.capturedEvents.last()).isEqualTo(RoomModeration(RoomModeration.Action.ResetPermissions))
        }
    }

    @Test
    fun `present - ResetPermissions confirmation can be cancelled`() = runTest {
        val presenter = createRolesAndPermissionsPresenter()
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(RolesAndPermissionsEvents.ResetPermissions)
            awaitItem().eventSink(RolesAndPermissionsEvents.CancelPendingAction)

            assertThat(awaitItem().resetPermissionsAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `present - admins and moderator counts are updated when members changes`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(updateMembersResult = {}),
        )
        val presenter = createRolesAndPermissionsPresenter(room = room)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.adminCount).isNull()
            assertThat(initialState.moderatorCount).isNull()
            room.givenRoomMembersState(state = RoomMembersState.Ready(aRoomMemberList()))
            skipItems(1)
            val finalState = awaitItem()
            assertThat(finalState.adminCount).isEqualTo(1)
            assertThat(finalState.moderatorCount).isEqualTo(1)
        }
    }

    private fun TestScope.createRolesAndPermissionsPresenter(
        room: FakeJoinedRoom = FakeJoinedRoom(baseRoom = FakeBaseRoom(updateMembersResult = {})),
        dispatchers: CoroutineDispatchers = testCoroutineDispatchers(),
        analyticsService: FakeAnalyticsService = FakeAnalyticsService()
    ): RolesAndPermissionsPresenter {
        return RolesAndPermissionsPresenter(
            room = room,
            dispatchers = dispatchers,
            analyticsService = analyticsService
        )
    }
}
