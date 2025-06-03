/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roommembermoderation.impl

import app.cash.turbine.TurbineTestContext
import com.google.common.truth.Truth.assertThat
import io.element.android.features.roommembermoderation.api.ModerationAction
import io.element.android.features.roommembermoderation.api.ModerationActionState
import io.element.android.features.roommembermoderation.api.RoomMemberModerationEvents
import io.element.android.features.roommembermoderation.api.RoomMemberModerationState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembersState
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class RoomMemberModerationPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val targetUser = MatrixUser(userId = A_USER_ID)

    @Test
    fun `present - initial state`() = runTest {
        val room = aJoinedRoom()
        createRoomMemberModerationPresenter(room = room).test {
            val initialState = awaitState()
            assertThat(initialState.canKick).isFalse()
            assertThat(initialState.canBan).isFalse()
            assertThat(initialState.selectedUser).isNull()
            assertThat(initialState.banUserAsyncAction).isEqualTo(AsyncAction.Uninitialized)
            assertThat(initialState.kickUserAsyncAction).isEqualTo(AsyncAction.Uninitialized)
            assertThat(initialState.unbanUserAsyncAction).isEqualTo(AsyncAction.Uninitialized)
            assertThat(initialState.actions).isEmpty()
        }
    }

    @Test
    fun `present - show actions when canBan=false, canKick=false`() = runTest {
        val room = aJoinedRoom(
            canBan = false,
            canKick = false,
            myUserRole = RoomMember.Role.USER,
            targetRoomMember = aRoomMember(userId = A_USER_ID, powerLevel = RoomMember.Role.USER.powerLevel)
        )
        createRoomMemberModerationPresenter(room = room).test {
            val initialState = awaitState()
            initialState.eventSink(RoomMemberModerationEvents.ShowActionsForUser(targetUser))
            skipItems(1)
            val updatedState = awaitState()
            assertThat(updatedState.selectedUser).isEqualTo(targetUser)
            assertThat(updatedState.actions).containsExactly(
                ModerationActionState(action = ModerationAction.DisplayProfile, isEnabled = true),
            )
        }
    }

    @Test
    fun `present - show actions when canBan=true, canKick=true, userRole=Admin and target member is unknown`() = runTest {
        val room = aJoinedRoom(
            canBan = true,
            canKick = true,
            myUserRole = RoomMember.Role.ADMIN,
            targetRoomMember = null
        )
        createRoomMemberModerationPresenter(room = room).test {
            val initialState = awaitState()
            initialState.eventSink(RoomMemberModerationEvents.ShowActionsForUser(targetUser))
            skipItems(2)
            val updatedState = awaitState()
            assertThat(updatedState.selectedUser).isEqualTo(targetUser)
            assertThat(updatedState.actions).containsExactly(
                ModerationActionState(action = ModerationAction.DisplayProfile, isEnabled = true),
                ModerationActionState(action = ModerationAction.KickUser, isEnabled = true),
                ModerationActionState(action = ModerationAction.BanUser, isEnabled = true),
            )
        }
    }

    @Test
    fun `show actions when canBan=true, canKick=true, userRole=Admin and target is User`() = runTest {
        val room = aJoinedRoom(
            canBan = true,
            canKick = true,
            myUserRole = RoomMember.Role.ADMIN,
            targetRoomMember = aRoomMember(userId = A_USER_ID, powerLevel = RoomMember.Role.USER.powerLevel)
        )
        createRoomMemberModerationPresenter(room = room).test {
            val initialState = awaitState()
            initialState.eventSink(RoomMemberModerationEvents.ShowActionsForUser(targetUser))
            skipItems(2)
            val updatedState = awaitState()
            assertThat(updatedState.selectedUser).isEqualTo(targetUser)
            assertThat(updatedState.actions).containsExactly(
                ModerationActionState(action = ModerationAction.DisplayProfile, isEnabled = true),
                ModerationActionState(action = ModerationAction.KickUser, isEnabled = true),
                ModerationActionState(action = ModerationAction.BanUser, isEnabled = true),
            )
        }
    }

    @Test
    fun `show actions when canBan=true, canKick=true, userRole=Moderator and target is Admin`() = runTest {
        val room = aJoinedRoom(
            canBan = true,
            canKick = true,
            myUserRole = RoomMember.Role.MODERATOR,
            targetRoomMember = aRoomMember(userId = A_USER_ID, powerLevel = RoomMember.Role.ADMIN.powerLevel)
        )
        createRoomMemberModerationPresenter(room = room).test {
            val initialState = awaitState()
            initialState.eventSink(RoomMemberModerationEvents.ShowActionsForUser(targetUser))
            skipItems(2)
            val updatedState = awaitState()
            assertThat(updatedState.selectedUser).isEqualTo(targetUser)
            assertThat(updatedState.actions).containsExactly(
                ModerationActionState(action = ModerationAction.DisplayProfile, isEnabled = true),
                ModerationActionState(action = ModerationAction.KickUser, isEnabled = false),
                ModerationActionState(action = ModerationAction.BanUser, isEnabled = false),
            )
        }
    }

    @Test
    fun `show actions when canBan=true, canKick=true, userRole=Moderator and target is Banned`() = runTest {
        val room = aJoinedRoom(
            canBan = true,
            canKick = true,
            myUserRole = RoomMember.Role.MODERATOR,
            targetRoomMember = aRoomMember(userId = A_USER_ID, membership = RoomMembershipState.BAN)
        )
        createRoomMemberModerationPresenter(room = room).test {
            val initialState = awaitState()
            initialState.eventSink(RoomMemberModerationEvents.ShowActionsForUser(targetUser))
            skipItems(2)
            val updatedState = awaitState()
            assertThat(updatedState.selectedUser).isEqualTo(targetUser)
            assertThat(updatedState.actions).containsExactly(
                ModerationActionState(action = ModerationAction.DisplayProfile, isEnabled = true),
                ModerationActionState(action = ModerationAction.KickUser, isEnabled = false),
                ModerationActionState(action = ModerationAction.UnbanUser, isEnabled = true),
            )
        }
    }

    @Test
    fun `present - process kick action sets confirming state`() = runTest {
        createRoomMemberModerationPresenter(room = aJoinedRoom()).test {
            val initialState = awaitState()
            initialState.eventSink(
                RoomMemberModerationEvents.ProcessAction(
                    targetUser = targetUser,
                    action = ModerationAction.KickUser
                )
            )
            skipItems(1)
            val updatedState = awaitState()
            assertThat(updatedState.selectedUser).isEqualTo(targetUser)
            assertThat(updatedState.kickUserAsyncAction).isEqualTo(AsyncAction.ConfirmingNoParams)
        }
    }

    @Test
    fun `present - process ban action sets confirming state`() = runTest {
        createRoomMemberModerationPresenter(room = aJoinedRoom()).test {
            val initialState = awaitState()
            initialState.eventSink(
                RoomMemberModerationEvents.ProcessAction(
                    targetUser = targetUser,
                    action = ModerationAction.BanUser
                )
            )
            skipItems(1)
            val updatedState = awaitState()
            assertThat(updatedState.selectedUser).isEqualTo(targetUser)
            assertThat(updatedState.banUserAsyncAction).isEqualTo(AsyncAction.ConfirmingNoParams)
        }
    }

    @Test
    fun `present - process unban action sets confirming state`() = runTest {
        createRoomMemberModerationPresenter(room = aJoinedRoom()).test {
            val initialState = awaitState()
            initialState.eventSink(
                RoomMemberModerationEvents.ProcessAction(
                    targetUser = targetUser,
                    action = ModerationAction.UnbanUser
                )
            )
            skipItems(1)
            val updatedState = awaitState()
            assertThat(updatedState.selectedUser).isEqualTo(targetUser)
            assertThat(updatedState.unbanUserAsyncAction).isEqualTo(AsyncAction.ConfirmingNoParams)
        }
    }

    @Test
    fun `present - do kick user with success`() = runTest {
        createRoomMemberModerationPresenter(room = aJoinedRoom()).test {
            val initialState = awaitState()
            initialState.eventSink(
                RoomMemberModerationEvents.ProcessAction(
                    targetUser = targetUser,
                    action = ModerationAction.KickUser
                )
            )
            skipItems(2)
            initialState.eventSink(InternalRoomMemberModerationEvents.DoKickUser("Reason"))
            skipItems(1)
            val loadingState = awaitState()
            assertThat(loadingState.kickUserAsyncAction).isInstanceOf(AsyncAction.Loading::class.java)
            val successState = awaitState()
            assertThat(successState.kickUserAsyncAction).isInstanceOf(AsyncAction.Success::class.java)
            assertThat(successState.selectedUser).isNull()
        }
    }

    @Test
    fun `present - do ban user with success`() = runTest {
        createRoomMemberModerationPresenter(room = aJoinedRoom()).test {
            val initialState = awaitState()
            initialState.eventSink(
                RoomMemberModerationEvents.ProcessAction(
                    targetUser = targetUser,
                    action = ModerationAction.BanUser
                )
            )
            skipItems(2)
            initialState.eventSink(InternalRoomMemberModerationEvents.DoBanUser("Reason"))
            skipItems(1)
            val loadingState = awaitState()
            assertThat(loadingState.banUserAsyncAction).isInstanceOf(AsyncAction.Loading::class.java)
            val successState = awaitState()
            assertThat(successState.banUserAsyncAction).isInstanceOf(AsyncAction.Success::class.java)
            assertThat(successState.selectedUser).isNull()
        }
    }

    @Test
    fun `present - do unban user with success`() = runTest {
        createRoomMemberModerationPresenter(room = aJoinedRoom()).test {
            val initialState = awaitState()
            initialState.eventSink(
                RoomMemberModerationEvents.ProcessAction(
                    targetUser = targetUser,
                    action = ModerationAction.UnbanUser
                )
            )
            skipItems(2)
            initialState.eventSink(InternalRoomMemberModerationEvents.DoUnbanUser)
            skipItems(1)
            val loadingState = awaitState()
            assertThat(loadingState.unbanUserAsyncAction).isInstanceOf(AsyncAction.Loading::class.java)
            val successState = awaitState()
            assertThat(successState.unbanUserAsyncAction).isInstanceOf(AsyncAction.Success::class.java)
            assertThat(successState.selectedUser).isNull()
        }
    }

    @Test
    fun `present - do kick user with failure`() = runTest {
        val error = RuntimeException("Test error")
        val room = aJoinedRoom(
            kickUserResult = Result.failure(error),
        )
        createRoomMemberModerationPresenter(room = room).test {
            val initialState = awaitState()
            initialState.eventSink(
                RoomMemberModerationEvents.ProcessAction(
                    targetUser = targetUser,
                    action = ModerationAction.KickUser
                )
            )
            skipItems(2)
            initialState.eventSink(InternalRoomMemberModerationEvents.DoKickUser("Reason"))
            skipItems(1)
            val loadingState = awaitState()
            assertThat(loadingState.kickUserAsyncAction).isInstanceOf(AsyncAction.Loading::class.java)
            val failureState = awaitState()
            assertThat(failureState.kickUserAsyncAction).isInstanceOf(AsyncAction.Failure::class.java)
        }
    }

    @Test
    fun `present - reset clears all async actions and selected user`() = runTest {
        createRoomMemberModerationPresenter(room = aJoinedRoom()).test {
            val initialState = awaitState()
            initialState.eventSink(
                RoomMemberModerationEvents.ProcessAction(targetUser = targetUser, action = ModerationAction.BanUser)
            )
            skipItems(2)
            initialState.eventSink(InternalRoomMemberModerationEvents.Reset)
            skipItems(1)
            val resetState = awaitState()
            assertThat(resetState.selectedUser).isNull()
            assertThat(resetState.banUserAsyncAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    private fun aJoinedRoom(
        canKick: Boolean = false,
        canBan: Boolean = false,
        myUserRole: RoomMember.Role = RoomMember.Role.USER,
        kickUserResult: Result<Unit> = Result.success(Unit),
        banUserResult: Result<Unit> = Result.success(Unit),
        unBanUserResult: Result<Unit> = Result.success(Unit),
        targetRoomMember: RoomMember? = null,
    ): JoinedRoom {
        return FakeJoinedRoom(
            kickUserResult = { _, _ -> kickUserResult },
            banUserResult = { _, _ -> banUserResult },
            unBanUserResult = { _, _ -> unBanUserResult },
            baseRoom = FakeBaseRoom(
                canBanResult = { _ -> Result.success(canBan) },
                canKickResult = { _ -> Result.success(canKick) },
                userRoleResult = { Result.success(myUserRole) },
            ),
        ).apply {
            val roomMembers = listOfNotNull(targetRoomMember).toPersistentList()
            givenRoomMembersState(state = RoomMembersState.Ready(roomMembers))
        }
    }

    private fun TestScope.createRoomMemberModerationPresenter(
        room: JoinedRoom,
        dispatchers: CoroutineDispatchers = testCoroutineDispatchers(),
        analyticsService: AnalyticsService = FakeAnalyticsService(),
    ): RoomMemberModerationPresenter {
        return RoomMemberModerationPresenter(
            room = room,
            dispatchers = dispatchers,
            analyticsService = analyticsService,
        )
    }

    private suspend fun TurbineTestContext<RoomMemberModerationState>.awaitState(): InternalRoomMemberModerationState {
        return awaitItem() as InternalRoomMemberModerationState
    }
}
