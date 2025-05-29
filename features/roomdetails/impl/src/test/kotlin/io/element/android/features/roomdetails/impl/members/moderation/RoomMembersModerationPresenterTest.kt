/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members.moderation

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.RoomModeration
import io.element.android.features.roomdetails.impl.aJoinedRoom
import io.element.android.features.roomdetails.impl.members.aRoomMember
import io.element.android.features.roomdetails.impl.members.aVictor
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembersState
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.test.A_REASON
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoomMembersModerationPresenterTest {
    @Test
    fun `canDisplayModerationActions - when room is DM is false`() = runTest {
        val room = aJoinedRoom(
            isPublic = true,
            activeMemberCount = 2,
            canKickResult = { Result.success(true) },
            canBanResult = { Result.success(true) },
            userRoleResult = { Result.success(RoomMember.Role.ADMIN) },
        ).apply {
            givenRoomInfo(aRoomInfo(isDirect = true, activeMembersCount = 2))
        }
        val presenter = createRoomMembersModerationPresenter(joinedRoom = room)
        presenter.test {
            assertThat(awaitItem().canDisplayModerationActions).isFalse()
        }
    }

    @Test
    fun `canDisplayModerationActions - when user can kick other users, FF is enabled and room is not a DM returns true`() = runTest {
        val room = aJoinedRoom(
            activeMemberCount = 10,
            canKickResult = { Result.success(true) },
            canBanResult = { Result.success(true) },
            userRoleResult = { Result.success(RoomMember.Role.ADMIN) },
        )
        val presenter = createRoomMembersModerationPresenter(joinedRoom = room)
        presenter.test {
            skipItems(1)
            assertThat(awaitItem().canDisplayModerationActions).isTrue()
        }
    }

    @Test
    fun `canDisplayModerationActions - when user can ban other users, FF is enabled and room is not a DM returns true`() = runTest {
        val room = aJoinedRoom(
            activeMemberCount = 10,
            canKickResult = { Result.success(true) },
            canBanResult = { Result.success(true) },
            userRoleResult = { Result.success(RoomMember.Role.ADMIN) },
        )
        val presenter = createRoomMembersModerationPresenter(joinedRoom = room)
        presenter.test {
            skipItems(1)
            assertThat(awaitItem().canDisplayModerationActions).isTrue()
        }
    }

    @Test
    fun `present - SelectRoomMember when the current user has permissions displays member actions`() = runTest {
        val room = aJoinedRoom(
            canKickResult = { Result.success(true) },
            canBanResult = { Result.success(true) },
            userRoleResult = { Result.success(RoomMember.Role.ADMIN) },
        )
        val selectedMember = aVictor()
        val presenter = createRoomMembersModerationPresenter(joinedRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            awaitItem().eventSink(RoomMembersModerationEvents.SelectRoomMember(selectedMember))
            with(awaitItem()) {
                assertThat(this.selectedRoomMember).isNotNull()
                assertThat(this.selectedRoomMember?.userId).isEqualTo(selectedMember.userId)
                assertThat(actions).containsExactly(
                    ModerationAction.DisplayProfile(selectedMember.userId),
                    ModerationAction.KickUser(selectedMember.userId),
                    ModerationAction.BanUser(selectedMember.userId)
                )
            }
        }
    }

    @Test
    fun `present - SelectRoomMember displays only view profile if selected member has same power level as the current user`() = runTest {
        val room = aJoinedRoom(
            sessionId = A_USER_ID,
            canKickResult = { Result.success(true) },
            canBanResult = { Result.success(true) },
            userRoleResult = { Result.success(RoomMember.Role.ADMIN) },
        )
        val selectedMember = aRoomMember(A_USER_ID_2, powerLevel = 100L)
        val presenter = createRoomMembersModerationPresenter(joinedRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            awaitItem().eventSink(RoomMembersModerationEvents.SelectRoomMember(selectedMember))
            with(awaitItem()) {
                assertThat(this.selectedRoomMember).isNotNull()
                assertThat(this.selectedRoomMember?.userId).isEqualTo(selectedMember.userId)
                assertThat(actions).containsExactly(
                    ModerationAction.DisplayProfile(selectedMember.userId),
                )
            }
        }
    }

    @Test
    fun `present - SelectRoomMember displays an unban confirmation dialog when the member is banned`() = runTest {
        val selectedMember = aRoomMember(A_USER_ID_2, membership = RoomMembershipState.BAN)
        val room = aJoinedRoom(
            canKickResult = { Result.success(true) },
            canBanResult = { Result.success(true) },
            userRoleResult = { Result.success(RoomMember.Role.ADMIN) },
        )
        val presenter = createRoomMembersModerationPresenter(joinedRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            awaitItem().eventSink(RoomMembersModerationEvents.SelectRoomMember(selectedMember))
            with(awaitItem()) {
                assertThat(selectedRoomMember).isNull()
                assertThat(unbanUserAsyncAction).isEqualTo(ConfirmingRoomMemberAction(selectedMember))
            }
        }
    }

    @Test
    fun `present - Kick requires confirmation and then kicks the user`() = runTest {
        val analyticsService = FakeAnalyticsService()
        val kickUserResult = lambdaRecorder<UserId, String?, Result<Unit>> { _, _ -> Result.success(Unit) }
        val room = aJoinedRoom(
            canKickResult = { Result.success(true) },
            canBanResult = { Result.success(true) },
            userRoleResult = { Result.success(RoomMember.Role.ADMIN) },
            kickUserResult = kickUserResult,
        )
        val selectedMember = aVictor()
        val presenter = createRoomMembersModerationPresenter(joinedRoom = room, analyticsService = analyticsService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            awaitItem().eventSink(RoomMembersModerationEvents.SelectRoomMember(selectedMember))
            awaitItem().eventSink(RoomMembersModerationEvents.KickUser)
            val confirmingState = awaitItem()
            assertThat(confirmingState.kickUserAsyncAction).isEqualTo(AsyncAction.ConfirmingNoParams)
            // Confirm
            confirmingState.eventSink(RoomMembersModerationEvents.DoKickUser(reason = A_REASON))
            skipItems(1)
            val loadingState = awaitItem()
            assertThat(loadingState.actions).isEmpty()
            assertThat(loadingState.kickUserAsyncAction).isEqualTo(AsyncAction.Loading)
            with(awaitItem()) {
                assertThat(kickUserAsyncAction).isEqualTo(AsyncAction.Success(Unit))
                assertThat(selectedRoomMember).isNull()
            }
            assertThat(analyticsService.capturedEvents.last()).isEqualTo(RoomModeration(RoomModeration.Action.KickMember))
            kickUserResult.assertions().isCalledOnce().with(
                value(selectedMember.userId),
                value(A_REASON),
            )
        }
    }

    @Test
    fun `present - BanUser requires confirmation and then bans the user`() = runTest {
        val analyticsService = FakeAnalyticsService()
        val banUserResult = lambdaRecorder<UserId, String?, Result<Unit>> { _, _ -> Result.success(Unit) }
        val room = aJoinedRoom(
            canKickResult = { Result.success(true) },
            canBanResult = { Result.success(true) },
            userRoleResult = { Result.success(RoomMember.Role.ADMIN) },
            banUserResult = banUserResult,
        )
        val selectedMember = aVictor()
        val presenter = createRoomMembersModerationPresenter(joinedRoom = room, analyticsService = analyticsService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            awaitItem().eventSink(RoomMembersModerationEvents.SelectRoomMember(selectedMember))
            awaitItem().eventSink(RoomMembersModerationEvents.BanUser)
            val confirmingState = awaitItem()
            assertThat(confirmingState.banUserAsyncAction).isEqualTo(AsyncAction.ConfirmingNoParams)
            // Confirm
            confirmingState.eventSink(RoomMembersModerationEvents.DoBanUser(reason = A_REASON))
            skipItems(1)
            val loadingItem = awaitItem()
            assertThat(loadingItem.actions).isEmpty()
            assertThat(loadingItem.selectedRoomMember).isNull()
            assertThat(loadingItem.banUserAsyncAction).isEqualTo(AsyncAction.Loading)
            with(awaitItem()) {
                assertThat(banUserAsyncAction).isEqualTo(AsyncAction.Success(Unit))
                assertThat(selectedRoomMember).isNull()
            }
            assertThat(analyticsService.capturedEvents.last()).isEqualTo(RoomModeration(RoomModeration.Action.BanMember))
            banUserResult.assertions().isCalledOnce().with(
                value(selectedMember.userId),
                value(A_REASON),
            )
        }
    }

    @Test
    fun `present - UnbanUser requires confirmation and then unbans the user`() = runTest {
        val analyticsService = FakeAnalyticsService()
        val selectedMember = aRoomMember(A_USER_ID_2, membership = RoomMembershipState.BAN)
        val room = aJoinedRoom(
            canKickResult = { Result.success(true) },
            canBanResult = { Result.success(true) },
            userRoleResult = { Result.success(RoomMember.Role.ADMIN) },
            unBanUserResult = { _, _ -> Result.success(Unit) },
        ).apply {
            givenRoomMembersState(RoomMembersState.Ready(persistentListOf(selectedMember)))
        }
        val presenter = createRoomMembersModerationPresenter(joinedRoom = room, analyticsService = analyticsService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            // Displays unban confirmation dialog
            awaitItem().eventSink(RoomMembersModerationEvents.SelectRoomMember(selectedMember))
            val confirmingState = awaitItem()
            assertThat(confirmingState.selectedRoomMember).isNull()
            assertThat(confirmingState.actions).isEmpty()
            assertThat(confirmingState.unbanUserAsyncAction).isEqualTo(ConfirmingRoomMemberAction(selectedMember))
            // Confirms unban
            confirmingState.eventSink(RoomMembersModerationEvents.UnbanUser(selectedMember.userId))
            assertThat(awaitItem().unbanUserAsyncAction).isEqualTo(AsyncAction.Loading)
            with(awaitItem()) {
                assertThat(unbanUserAsyncAction).isEqualTo(AsyncAction.Success(Unit))
                assertThat(selectedRoomMember).isNull()
            }
            assertThat(analyticsService.capturedEvents.last()).isEqualTo(RoomModeration(RoomModeration.Action.UnbanMember))
        }
    }

    @Test
    fun `present - Reset removes the selected user and actions`() = runTest {
        val room = aJoinedRoom(
            canKickResult = { Result.success(true) },
            canBanResult = { Result.success(true) },
            userRoleResult = { Result.success(RoomMember.Role.USER) },
        )
        val presenter = createRoomMembersModerationPresenter(joinedRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            // Select a user
            awaitItem().eventSink(RoomMembersModerationEvents.SelectRoomMember(aVictor()))
            // Reset state
            awaitItem().eventSink(RoomMembersModerationEvents.Reset)
            val finalItem = awaitItem()
            assertThat(finalItem.selectedRoomMember).isNull()
            assertThat(finalItem.actions).isEmpty()
        }
    }

    @Test
    fun `present - Reset resets any async actions`() = runTest {
        val room = aJoinedRoom(
            canKickResult = { Result.success(true) },
            canBanResult = { Result.success(true) },
            kickUserResult = { _, _ -> Result.failure(RuntimeException("Eek")) },
            banUserResult = { _, _ -> Result.failure(RuntimeException("Eek")) },
            unBanUserResult = { _, _ -> Result.failure(RuntimeException("Eek")) },
            userRoleResult = { Result.success(RoomMember.Role.USER) },
        )
        val presenter = createRoomMembersModerationPresenter(joinedRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialItem = awaitItem()
            // Kick user and fail
            awaitItem().eventSink(RoomMembersModerationEvents.SelectRoomMember(aVictor()))
            awaitItem().eventSink(RoomMembersModerationEvents.DoKickUser(reason = ""))
            skipItems(1)
            assertThat(awaitItem().kickUserAsyncAction).isInstanceOf(AsyncAction.Loading::class.java)
            assertThat(awaitItem().kickUserAsyncAction).isInstanceOf(AsyncAction.Failure::class.java)
            // Reset it
            initialItem.eventSink(RoomMembersModerationEvents.Reset)
            assertThat(awaitItem().kickUserAsyncAction).isEqualTo(AsyncAction.Uninitialized)

            // Ban user and fail
            initialItem.eventSink(RoomMembersModerationEvents.SelectRoomMember(aVictor()))
            awaitItem().eventSink(RoomMembersModerationEvents.DoBanUser(reason = ""))
            skipItems(1)
            assertThat(awaitItem().banUserAsyncAction).isInstanceOf(AsyncAction.Loading::class.java)
            assertThat(awaitItem().banUserAsyncAction).isInstanceOf(AsyncAction.Failure::class.java)
            // Reset it
            initialItem.eventSink(RoomMembersModerationEvents.Reset)
            assertThat(awaitItem().banUserAsyncAction).isEqualTo(AsyncAction.Uninitialized)

            // Unban user and fail
            initialItem.eventSink(RoomMembersModerationEvents.SelectRoomMember(aVictor().copy(membership = RoomMembershipState.BAN)))
            val confirmingState = awaitItem()
            assertThat(confirmingState.unbanUserAsyncAction).isInstanceOf(AsyncAction.Confirming::class.java)
            confirmingState.eventSink(RoomMembersModerationEvents.UnbanUser(aVictor().userId))
            assertThat(awaitItem().unbanUserAsyncAction).isInstanceOf(AsyncAction.Loading::class.java)
            assertThat(awaitItem().unbanUserAsyncAction).isInstanceOf(AsyncAction.Failure::class.java)
            // Reset it
            initialItem.eventSink(RoomMembersModerationEvents.Reset)
            assertThat(awaitItem().unbanUserAsyncAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    private fun TestScope.createRoomMembersModerationPresenter(
        joinedRoom: FakeJoinedRoom = aJoinedRoom(),
        dispatchers: CoroutineDispatchers = testCoroutineDispatchers(),
        analyticsService: FakeAnalyticsService = FakeAnalyticsService(),
    ): RoomMembersModerationPresenter {
        return RoomMembersModerationPresenter(
            room = joinedRoom,
            dispatchers = dispatchers,
            analyticsService = analyticsService,
        )
    }
}
