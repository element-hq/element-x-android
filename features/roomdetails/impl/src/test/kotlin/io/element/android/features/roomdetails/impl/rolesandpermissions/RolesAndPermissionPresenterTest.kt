/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.rolesandpermissions

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.RoomModeration
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.defaultRoomPowerLevels
import io.element.android.services.analytics.test.FakeAnalyticsService
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
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            with(awaitItem()) {
                assertThat(adminCount).isEqualTo(0)
                assertThat(moderatorCount).isEqualTo(0)
                assertThat(changeOwnRoleAction).isEqualTo(AsyncAction.Uninitialized)
            }
        }
    }

    @Test
    fun `present - ChangeOwnRole presents a confirmation dialog`() = runTest {
        val presenter = createRolesAndPermissionsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
                updateUserRoleResult = { Result.success(Unit) }
            ),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(RolesAndPermissionsEvents.DemoteSelfTo(RoomMember.Role.MODERATOR))

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
            updateUserRoleResult = { Result.failure(Exception("Failed to update role")) }
        )
        val presenter = createRolesAndPermissionsPresenter(room = room, dispatchers = testCoroutineDispatchers())
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(RolesAndPermissionsEvents.DemoteSelfTo(RoomMember.Role.MODERATOR))

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
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
                resetPowerLevelsResult = { Result.success(defaultRoomPowerLevels()) }
            )
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(RolesAndPermissionsEvents.ResetPermissions)
            awaitItem().eventSink(RolesAndPermissionsEvents.CancelPendingAction)

            assertThat(awaitItem().resetPermissionsAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    private fun TestScope.createRolesAndPermissionsPresenter(
        room: FakeJoinedRoom = FakeJoinedRoom(),
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
