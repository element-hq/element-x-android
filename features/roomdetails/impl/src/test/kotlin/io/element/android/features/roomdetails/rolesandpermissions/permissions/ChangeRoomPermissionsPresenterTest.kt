/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.rolesandpermissions.permissions

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.Event
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.RoomModeration
import io.element.android.features.roomdetails.impl.rolesandpermissions.permissions.ChangeRoomPermissionsEvent
import io.element.android.features.roomdetails.impl.rolesandpermissions.permissions.ChangeRoomPermissionsPresenter
import io.element.android.features.roomdetails.impl.rolesandpermissions.permissions.ChangeRoomPermissionsSection
import io.element.android.features.roomdetails.impl.rolesandpermissions.permissions.ChangeRoomPermissionsState
import io.element.android.features.roomdetails.impl.rolesandpermissions.permissions.RoomPermissionType
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.room.RoomMember.Role.ADMIN
import io.element.android.libraries.matrix.api.room.RoomMember.Role.MODERATOR
import io.element.android.libraries.matrix.api.room.RoomMember.Role.USER
import io.element.android.libraries.matrix.api.room.powerlevels.MatrixRoomPowerLevels
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.defaultRoomPowerLevels
import io.element.android.services.analytics.test.FakeAnalyticsService
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ChangeRoomPermissionsPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val section = ChangeRoomPermissionsSection.RoomDetails
        val presenter = createChangeRoomPermissionsPresenter(section = section)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            // Initial state, no permissions loaded
            awaitItem().run {
                assertThat(this.section).isEqualTo(section)
                assertThat(this.currentPermissions).isNull()
                assertThat(this.items).isNotEmpty()
                assertThat(this.hasChanges).isFalse()
                assertThat(this.saveAction).isEqualTo(AsyncAction.Uninitialized)
                assertThat(this.confirmExitAction).isEqualTo(AsyncAction.Uninitialized)
            }

            // Updated state, permissions loaded
            assertThat(awaitItem().currentPermissions).isEqualTo(defaultPermissions())
        }
    }

    @Test
    fun `present - RoomDetails section contains the right items`() = runTest {
        val section = ChangeRoomPermissionsSection.RoomDetails
        val presenter = createChangeRoomPermissionsPresenter(section = section)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitUpdatedItem().items).containsExactly(
                RoomPermissionType.ROOM_NAME,
                RoomPermissionType.ROOM_AVATAR,
                RoomPermissionType.ROOM_TOPIC,
            )
        }
    }

    @Test
    fun `present - MessagesAndContent section contains the right items`() = runTest {
        val section = ChangeRoomPermissionsSection.MessagesAndContent
        val presenter = createChangeRoomPermissionsPresenter(section = section)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitUpdatedItem().items).containsExactly(
                RoomPermissionType.SEND_EVENTS,
                RoomPermissionType.REDACT_EVENTS,
            )
        }
    }

    @Test
    fun `present - MembershipModeration section contains the right items`() = runTest {
        val section = ChangeRoomPermissionsSection.MembershipModeration
        val presenter = createChangeRoomPermissionsPresenter(section = section)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitUpdatedItem().items).containsExactly(
                RoomPermissionType.INVITE,
                RoomPermissionType.KICK,
                RoomPermissionType.BAN,
            )
        }
    }

    @Test
    fun `present - ChangeMinimumRoleForAction updates the current permissions and hasChanges`() = runTest {
        val presenter = createChangeRoomPermissionsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = awaitUpdatedItem()
            assertThat(state.currentPermissions?.roomName).isEqualTo(ADMIN.powerLevel)
            assertThat(state.hasChanges).isFalse()

            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.ROOM_NAME, MODERATOR))

            awaitItem().run {
                assertThat(currentPermissions?.roomName).isEqualTo(MODERATOR.powerLevel)
                assertThat(hasChanges).isTrue()
            }
        }
    }

    @Test
    fun `present - ChangeMinimumRoleForAction works for all actions`() = runTest {
        val presenter = createChangeRoomPermissionsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = awaitUpdatedItem()

            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.INVITE, MODERATOR))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.KICK, MODERATOR))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.BAN, MODERATOR))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.SEND_EVENTS, MODERATOR))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.REDACT_EVENTS, MODERATOR))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.ROOM_NAME, MODERATOR))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.ROOM_AVATAR, MODERATOR))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.ROOM_TOPIC, MODERATOR))

            val items = cancelAndConsumeRemainingEvents()

            (items.last() as? Event.Item<ChangeRoomPermissionsState>)?.value?.run {
                assertThat(currentPermissions).isEqualTo(
                    MatrixRoomPowerLevels(
                        invite = MODERATOR.powerLevel,
                        kick = MODERATOR.powerLevel,
                        ban = MODERATOR.powerLevel,
                        redactEvents = MODERATOR.powerLevel,
                        sendEvents = MODERATOR.powerLevel,
                        roomName = MODERATOR.powerLevel,
                        roomAvatar = MODERATOR.powerLevel,
                        roomTopic = MODERATOR.powerLevel,
                    )
                )
            }
        }
    }

    @Test
    fun `present - Save updates the current permissions and resets hasChanges`() = runTest {
        val analyticsService = FakeAnalyticsService()
        val presenter = createChangeRoomPermissionsPresenter(
            analyticsService = analyticsService,
            room = FakeMatrixRoom(
                updatePowerLevelsResult = { Result.success(Unit) },
                powerLevelsResult = { Result.success(defaultPermissions()) }
            ),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = awaitUpdatedItem()
            assertThat(state.currentPermissions?.roomName).isEqualTo(ADMIN.powerLevel)
            assertThat(state.hasChanges).isFalse()

            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.ROOM_NAME, MODERATOR))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.ROOM_AVATAR, MODERATOR))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.ROOM_TOPIC, MODERATOR))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.SEND_EVENTS, MODERATOR))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.REDACT_EVENTS, USER))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.KICK, ADMIN))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.BAN, ADMIN))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.INVITE, ADMIN))
            skipItems(7)
            assertThat(awaitItem().hasChanges).isTrue()

            state.eventSink(ChangeRoomPermissionsEvent.Save)

            assertThat(awaitItem().saveAction).isEqualTo(AsyncAction.Loading)
            assertThat(awaitItem().hasChanges).isFalse()
            awaitItem().run {
                assertThat(currentPermissions?.roomName).isEqualTo(MODERATOR.powerLevel)
                assertThat(saveAction).isEqualTo(AsyncAction.Success(Unit))
            }
            assertThat(analyticsService.capturedEvents).containsExactlyElementsIn(
                listOf(
                    RoomModeration(RoomModeration.Action.ChangePermissionsRoomName, RoomModeration.Role.Moderator),
                    RoomModeration(RoomModeration.Action.ChangePermissionsRoomAvatar, RoomModeration.Role.Moderator),
                    RoomModeration(RoomModeration.Action.ChangePermissionsRoomTopic, RoomModeration.Role.Moderator),
                    RoomModeration(RoomModeration.Action.ChangePermissionsSendMessages, RoomModeration.Role.Moderator),
                    RoomModeration(RoomModeration.Action.ChangePermissionsRedactMessages, RoomModeration.Role.User),
                    RoomModeration(RoomModeration.Action.ChangePermissionsKickMembers, RoomModeration.Role.Administrator),
                    RoomModeration(RoomModeration.Action.ChangePermissionsBanMembers, RoomModeration.Role.Administrator),
                    RoomModeration(RoomModeration.Action.ChangePermissionsInviteUsers, RoomModeration.Role.Administrator),
                )
            )
        }
    }

    @Test
    fun `present - Save will fail if there are not current permissions`() = runTest {
        val room = FakeMatrixRoom(
            powerLevelsResult = { Result.failure(IllegalStateException("Failed to load power levels")) }
        )
        val presenter = createChangeRoomPermissionsPresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = awaitItem()
            assertThat(state.currentPermissions).isNull()

            state.eventSink(ChangeRoomPermissionsEvent.Save)
            assertThat(awaitItem().saveAction).isInstanceOf(AsyncAction.Failure::class.java)
        }
    }

    @Test
    fun `present - Save can handle failures and they can be cleared`() = runTest {
        val room = FakeMatrixRoom(
            powerLevelsResult = { Result.success(defaultPermissions()) },
            updatePowerLevelsResult = { Result.failure(IllegalStateException("Failed to update power levels")) },
        )
        val presenter = createChangeRoomPermissionsPresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = awaitUpdatedItem()
            assertThat(state.currentPermissions?.roomName).isEqualTo(ADMIN.powerLevel)
            assertThat(state.hasChanges).isFalse()

            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.ROOM_NAME, MODERATOR))
            assertThat(awaitItem().hasChanges).isTrue()

            state.eventSink(ChangeRoomPermissionsEvent.Save)

            assertThat(awaitItem().saveAction).isEqualTo(AsyncAction.Loading)
            awaitItem().run {
                assertThat(currentPermissions?.roomName).isEqualTo(MODERATOR.powerLevel)
                // Couldn't save the changes, so they're still pending
                assertThat(hasChanges).isTrue()
                assertThat(saveAction).isInstanceOf(AsyncAction.Failure::class.java)
            }

            state.eventSink(ChangeRoomPermissionsEvent.ResetPendingActions)
            awaitItem().run {
                assertThat(currentPermissions?.roomName).isEqualTo(MODERATOR.powerLevel)
                assertThat(saveAction).isEqualTo(AsyncAction.Uninitialized)
                assertThat(hasChanges).isTrue()
            }
        }
    }

    @Test
    fun `present - Exit does not need a confirmation when there are no pending changes`() = runTest {
        val presenter = createChangeRoomPermissionsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = awaitUpdatedItem()
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.ROOM_NAME, MODERATOR))
            assertThat(awaitItem().hasChanges).isTrue()

            state.eventSink(ChangeRoomPermissionsEvent.Exit)
            assertThat(awaitItem().confirmExitAction).isEqualTo(AsyncAction.ConfirmingNoParams)

            state.eventSink(ChangeRoomPermissionsEvent.Exit)
            assertThat(awaitItem().confirmExitAction).isEqualTo(AsyncAction.Success(Unit))
        }
    }

    @Test
    fun `present - Exit needs confirmation when there are pending changes`() = runTest {
        val presenter = createChangeRoomPermissionsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = awaitUpdatedItem()

            state.eventSink(ChangeRoomPermissionsEvent.Exit)

            assertThat(awaitItem().confirmExitAction).isEqualTo(AsyncAction.Success(Unit))
        }
    }

    private fun createChangeRoomPermissionsPresenter(
        section: ChangeRoomPermissionsSection = ChangeRoomPermissionsSection.RoomDetails,
        room: FakeMatrixRoom = FakeMatrixRoom(
            powerLevelsResult = { Result.success(defaultPermissions()) }
        ),
        analyticsService: FakeAnalyticsService = FakeAnalyticsService(),
    ) = ChangeRoomPermissionsPresenter(
        section = section,
        room = room,
        analyticsService = analyticsService,
    )

    private fun defaultPermissions() = defaultRoomPowerLevels().run {
        MatrixRoomPowerLevels(
            invite = invite,
            kick = kick,
            ban = ban,
            redactEvents = redactEvents,
            sendEvents = sendEvents,
            roomName = roomName,
            roomAvatar = roomAvatar,
            roomTopic = roomTopic,
        )
    }

    private suspend fun TurbineTestContext<ChangeRoomPermissionsState>.awaitUpdatedItem(): ChangeRoomPermissionsState {
        skipItems(1)
        return awaitItem()
    }
}
