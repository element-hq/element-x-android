/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.roomdetails.rolesandpermissions.permissions

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.roomdetails.impl.rolesandpermissions.permissions.ChangeRoomPermissionsEvent
import io.element.android.features.roomdetails.impl.rolesandpermissions.permissions.ChangeRoomPermissionsPresenter
import io.element.android.features.roomdetails.impl.rolesandpermissions.permissions.ChangeRoomPermissionsSection
import io.element.android.features.roomdetails.impl.rolesandpermissions.permissions.ChangeRoomPermissionsState
import io.element.android.features.roomdetails.impl.rolesandpermissions.permissions.RoomPermissionsItem
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ChangeRoomPermissionsPresenterTests {
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
                assertThat(this.currentPermissions).isEmpty()
                assertThat(this.items).isNotEmpty()
                assertThat(this.hasChanges).isFalse()
                assertThat(this.saveAction).isEqualTo(AsyncAction.Uninitialized)
                assertThat(this.confirmExitAction).isEqualTo(AsyncAction.Uninitialized)
            }

            // Updated state, permissions loaded
            assertThat(awaitItem().currentPermissions).isEqualTo(defaultPermissionsMap())
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
                RoomPermissionsItem.ROOM_NAME,
                RoomPermissionsItem.ROOM_AVATAR,
                RoomPermissionsItem.ROOM_TOPIC,
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
                RoomPermissionsItem.SEND_EVENTS,
                RoomPermissionsItem.REDACT_EVENTS,
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
                RoomPermissionsItem.INVITE,
                RoomPermissionsItem.KICK,
                RoomPermissionsItem.BAN,
            )
        }
    }

    @Test
    fun `present - ChangeRole updates the current permissions and hasChanges`() = runTest {
        val presenter = createChangeRoomPermissionsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = awaitUpdatedItem()
            assertThat(state.currentPermissions[RoomPermissionsItem.ROOM_NAME]).isEqualTo(RoomMember.Role.ADMIN)
            assertThat(state.hasChanges).isFalse()

            state.eventSink(ChangeRoomPermissionsEvent.ChangeRole(RoomPermissionsItem.ROOM_NAME, RoomMember.Role.MODERATOR))

            awaitItem().run {
                assertThat(currentPermissions[RoomPermissionsItem.ROOM_NAME]).isEqualTo(RoomMember.Role.MODERATOR)
                assertThat(hasChanges).isTrue()
            }
        }
    }

    @Test
    fun `present - Save updates the current permissions and resets hasChanges`() = runTest {
        val presenter = createChangeRoomPermissionsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = awaitUpdatedItem()
            assertThat(state.currentPermissions[RoomPermissionsItem.ROOM_NAME]).isEqualTo(RoomMember.Role.ADMIN)
            assertThat(state.hasChanges).isFalse()

            state.eventSink(ChangeRoomPermissionsEvent.ChangeRole(RoomPermissionsItem.ROOM_NAME, RoomMember.Role.MODERATOR))
            assertThat(awaitItem().hasChanges).isTrue()

            state.eventSink(ChangeRoomPermissionsEvent.Save)
            awaitItem().run {
                assertThat(currentPermissions[RoomPermissionsItem.ROOM_NAME]).isEqualTo(RoomMember.Role.MODERATOR)
                assertThat(saveAction).isEqualTo(AsyncAction.Success(Unit))
                assertThat(hasChanges).isFalse()
            }
        }
    }

    @Test
    fun `present - Save can handle failures and they can be cleared`() = runTest {
        val room = FakeMatrixRoom().apply {
            givenUpdatePowerLevelsResult(Result.failure(IllegalStateException("Failed to update power levels")))
        }
        val presenter = createChangeRoomPermissionsPresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = awaitUpdatedItem()
            assertThat(state.currentPermissions[RoomPermissionsItem.ROOM_NAME]).isEqualTo(RoomMember.Role.ADMIN)
            assertThat(state.hasChanges).isFalse()

            state.eventSink(ChangeRoomPermissionsEvent.ChangeRole(RoomPermissionsItem.ROOM_NAME, RoomMember.Role.MODERATOR))
            assertThat(awaitItem().hasChanges).isTrue()

            state.eventSink(ChangeRoomPermissionsEvent.Save)
            awaitItem().run {
                assertThat(currentPermissions[RoomPermissionsItem.ROOM_NAME]).isEqualTo(RoomMember.Role.MODERATOR)
                // Couldn't save the changes, so they're still pending
                assertThat(hasChanges).isTrue()
                assertThat(saveAction).isInstanceOf(AsyncAction.Failure::class.java)
            }

            state.eventSink(ChangeRoomPermissionsEvent.ResetPendingActions)
            awaitItem().run {
                assertThat(currentPermissions[RoomPermissionsItem.ROOM_NAME]).isEqualTo(RoomMember.Role.MODERATOR)
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
            state.eventSink(ChangeRoomPermissionsEvent.ChangeRole(RoomPermissionsItem.ROOM_NAME, RoomMember.Role.MODERATOR))
            assertThat(awaitItem().hasChanges).isTrue()

            state.eventSink(ChangeRoomPermissionsEvent.Exit)
            assertThat(awaitItem().confirmExitAction).isEqualTo(AsyncAction.Confirming)

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
        room: FakeMatrixRoom = FakeMatrixRoom(),
    ) = ChangeRoomPermissionsPresenter(
        section = section,
        room = room,
    )

    private fun defaultPermissionsMap() = persistentMapOf(
        RoomPermissionsItem.INVITE to RoomMember.Role.USER,
        RoomPermissionsItem.KICK to RoomMember.Role.MODERATOR,
        RoomPermissionsItem.BAN to RoomMember.Role.MODERATOR,
        RoomPermissionsItem.SEND_EVENTS to RoomMember.Role.USER,
        RoomPermissionsItem.REDACT_EVENTS to RoomMember.Role.MODERATOR,
        RoomPermissionsItem.ROOM_NAME to RoomMember.Role.ADMIN,
        RoomPermissionsItem.ROOM_AVATAR to RoomMember.Role.ADMIN,
        RoomPermissionsItem.ROOM_TOPIC to RoomMember.Role.ADMIN,
    )

    private suspend fun TurbineTestContext<ChangeRoomPermissionsState>.awaitUpdatedItem(): ChangeRoomPermissionsState {
        skipItems(1)
        return awaitItem()
    }
}
