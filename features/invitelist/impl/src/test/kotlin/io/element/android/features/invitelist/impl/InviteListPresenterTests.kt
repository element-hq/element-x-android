/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.invitelist.impl

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.room.RoomSummary
import io.element.android.libraries.matrix.api.room.RoomSummaryDetails
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.FakeRoomSummaryDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InviteListPresenterTests {

    @Test
    fun `present - starts empty, adds invites when received`() = runTest {
        val invitesDataSource = FakeRoomSummaryDataSource()
        val presenter = InviteListPresenter(
            FakeMatrixClient(
                sessionId = A_SESSION_ID,
                invitesDataSource = invitesDataSource,
            )
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.inviteList).isEmpty()

            invitesDataSource.postRoomSummary(
                listOf(
                    RoomSummary.Filled(
                        RoomSummaryDetails(
                            roomId = A_ROOM_ID,
                            name = A_ROOM_NAME,
                            avatarURLString = null,
                            isDirect = false,
                            lastMessage = null,
                            lastMessageTimestamp = null,
                            unreadNotificationCount = 0,
                        )
                    )
                )
            )

            val withInviteState = awaitItem()
            Truth.assertThat(withInviteState.inviteList.size).isEqualTo(1)
            Truth.assertThat(withInviteState.inviteList[0].roomId).isEqualTo(A_ROOM_ID)
            Truth.assertThat(withInviteState.inviteList[0].roomName).isEqualTo(A_ROOM_NAME)
        }
    }

    @Test
    fun `present - uses user ID and avatar for direct invites`() = runTest {
        val invitesDataSource = FakeRoomSummaryDataSource().withDirectChatInvitation()
        val presenter = InviteListPresenter(
            FakeMatrixClient(
                sessionId = A_SESSION_ID,
                invitesDataSource = invitesDataSource,
            )
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val withInviteState = awaitItem()
            Truth.assertThat(withInviteState.inviteList.size).isEqualTo(1)
            Truth.assertThat(withInviteState.inviteList[0].roomId).isEqualTo(A_ROOM_ID)
            Truth.assertThat(withInviteState.inviteList[0].roomAlias).isEqualTo(A_USER_ID.value)
            Truth.assertThat(withInviteState.inviteList[0].roomName).isEqualTo(A_ROOM_NAME)
            Truth.assertThat(withInviteState.inviteList[0].roomAvatarData).isEqualTo(
                AvatarData(
                    id = A_USER_ID.value,
                    name = A_USER_NAME,
                    url = AN_AVATAR_URL,
                )
            )
            Truth.assertThat(withInviteState.inviteList[0].sender).isNull()
        }
    }

    @Test
    fun `present - includes sender details for room invites`() = runTest {
        val invitesDataSource = FakeRoomSummaryDataSource().withRoomInvitation()

        val presenter = InviteListPresenter(
            FakeMatrixClient(
                sessionId = A_SESSION_ID,
                invitesDataSource = invitesDataSource,
            )
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val withInviteState = awaitItem()
            Truth.assertThat(withInviteState.inviteList.size).isEqualTo(1)
            Truth.assertThat(withInviteState.inviteList[0].sender?.displayName).isEqualTo(A_USER_NAME)
            Truth.assertThat(withInviteState.inviteList[0].sender?.userId).isEqualTo(A_USER_ID)
            Truth.assertThat(withInviteState.inviteList[0].sender?.avatarData).isEqualTo(
                AvatarData(
                    id = A_USER_ID.value,
                    name = A_USER_NAME,
                    url = AN_AVATAR_URL,
                )
            )
        }
    }

    @Test
    fun `present - shows confirm dialog for declining direct chat invites`() = runTest {
        val invitesDataSource = FakeRoomSummaryDataSource().withDirectChatInvitation()

        val presenter = InviteListPresenter(
            FakeMatrixClient(
                sessionId = A_SESSION_ID,
                invitesDataSource = invitesDataSource,
            )
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val originalState = awaitItem()
            originalState.eventSink(InviteListEvents.DeclineInvite(originalState.inviteList[0]))

            val newState = awaitItem()
            Truth.assertThat(newState.declineConfirmationDialog).isInstanceOf(InviteDeclineConfirmationDialog.Visible::class.java)

            val confirmDialog = newState.declineConfirmationDialog as InviteDeclineConfirmationDialog.Visible
            Truth.assertThat(confirmDialog.isDirect).isTrue()
            Truth.assertThat(confirmDialog.name).isEqualTo(A_ROOM_NAME)
        }
    }

    @Test
    fun `present - shows confirm dialog for declining room invites`() = runTest {
        val invitesDataSource = FakeRoomSummaryDataSource().withRoomInvitation()

        val presenter = InviteListPresenter(
            FakeMatrixClient(
                sessionId = A_SESSION_ID,
                invitesDataSource = invitesDataSource,
            )
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val originalState = awaitItem()
            originalState.eventSink(InviteListEvents.DeclineInvite(originalState.inviteList[0]))

            val newState = awaitItem()
            Truth.assertThat(newState.declineConfirmationDialog).isInstanceOf(InviteDeclineConfirmationDialog.Visible::class.java)

            val confirmDialog = newState.declineConfirmationDialog as InviteDeclineConfirmationDialog.Visible
            Truth.assertThat(confirmDialog.isDirect).isFalse()
            Truth.assertThat(confirmDialog.name).isEqualTo(A_ROOM_NAME)
        }
    }

    @Test
    fun `present - hides confirm dialog when cancelling`() = runTest {
        val invitesDataSource = FakeRoomSummaryDataSource().withRoomInvitation()

        val presenter = InviteListPresenter(
            FakeMatrixClient(
                sessionId = A_SESSION_ID,
                invitesDataSource = invitesDataSource,
            )
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val originalState = awaitItem()
            originalState.eventSink(InviteListEvents.DeclineInvite(originalState.inviteList[0]))

            skipItems(1)

            originalState.eventSink(InviteListEvents.CancelDeclineInvite)

            val newState = awaitItem()
            Truth.assertThat(newState.declineConfirmationDialog).isInstanceOf(InviteDeclineConfirmationDialog.Hidden::class.java)
        }
    }

    @Test
    fun `present - declines invite after confirming`() = runTest {
        val invitesDataSource = FakeRoomSummaryDataSource().withRoomInvitation()
        val client = FakeMatrixClient(
            sessionId = A_SESSION_ID,
            invitesDataSource = invitesDataSource,
        )
        val room = FakeMatrixRoom()
        val presenter = InviteListPresenter(client)
        client.givenGetRoomResult(A_ROOM_ID, room)

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val originalState = awaitItem()
            originalState.eventSink(InviteListEvents.DeclineInvite(originalState.inviteList[0]))

            skipItems(1)

            originalState.eventSink(InviteListEvents.ConfirmDeclineInvite)

            skipItems(2)

            Truth.assertThat(room.isInviteRejected).isTrue()
        }
    }

    @Test
    fun `present - declines invite after confirming and sets state on error`() = runTest {
        val invitesDataSource = FakeRoomSummaryDataSource().withRoomInvitation()
        val client = FakeMatrixClient(
            sessionId = A_SESSION_ID,
            invitesDataSource = invitesDataSource,
        )
        val room = FakeMatrixRoom()
        val presenter = InviteListPresenter(client)
        val ex = Throwable("Ruh roh!")
        room.givenRejectInviteResult(Result.failure(ex))
        client.givenGetRoomResult(A_ROOM_ID, room)

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val originalState = awaitItem()
            originalState.eventSink(InviteListEvents.DeclineInvite(originalState.inviteList[0]))

            skipItems(1)

            originalState.eventSink(InviteListEvents.ConfirmDeclineInvite)

            skipItems(1)

            val newState = awaitItem()

            Truth.assertThat(room.isInviteRejected).isTrue()
            Truth.assertThat(newState.declinedAction).isEqualTo(Async.Failure<Unit>(ex))
        }
    }

    @Test
    fun `present - dismisses declining error state`() = runTest {
        val invitesDataSource = FakeRoomSummaryDataSource().withRoomInvitation()
        val client = FakeMatrixClient(
            sessionId = A_SESSION_ID,
            invitesDataSource = invitesDataSource,
        )
        val room = FakeMatrixRoom()
        val presenter = InviteListPresenter(client)
        val ex = Throwable("Ruh roh!")
        room.givenRejectInviteResult(Result.failure(ex))
        client.givenGetRoomResult(A_ROOM_ID, room)

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val originalState = awaitItem()
            originalState.eventSink(InviteListEvents.DeclineInvite(originalState.inviteList[0]))

            skipItems(1)

            originalState.eventSink(InviteListEvents.ConfirmDeclineInvite)

            skipItems(2)

            originalState.eventSink(InviteListEvents.DismissDeclineError)

            val newState = awaitItem()

            Truth.assertThat(newState.declinedAction).isEqualTo(Async.Uninitialized)
        }
    }

    @Test
    fun `present - accepts invites and sets state on success`() = runTest {
        val invitesDataSource = FakeRoomSummaryDataSource().withRoomInvitation()
        val client = FakeMatrixClient(
            sessionId = A_SESSION_ID,
            invitesDataSource = invitesDataSource,
        )
        val room = FakeMatrixRoom()
        val presenter = InviteListPresenter(client)
        client.givenGetRoomResult(A_ROOM_ID, room)

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val originalState = awaitItem()
            originalState.eventSink(InviteListEvents.AcceptInvite(originalState.inviteList[0]))

            val newState = awaitItem()

            Truth.assertThat(room.isInviteAccepted).isTrue()
            Truth.assertThat(newState.acceptedAction).isEqualTo(Async.Success(A_ROOM_ID))
        }
    }

    @Test
    fun `present - accepts invites and sets state on error`() = runTest {
        val invitesDataSource = FakeRoomSummaryDataSource().withRoomInvitation()
        val client = FakeMatrixClient(
            sessionId = A_SESSION_ID,
            invitesDataSource = invitesDataSource,
        )
        val room = FakeMatrixRoom()
        val presenter = InviteListPresenter(client)
        val ex = Throwable("Ruh roh!")
        room.givenAcceptInviteResult(Result.failure(ex))
        client.givenGetRoomResult(A_ROOM_ID, room)

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val originalState = awaitItem()
            originalState.eventSink(InviteListEvents.AcceptInvite(originalState.inviteList[0]))

            val newState = awaitItem()

            Truth.assertThat(room.isInviteAccepted).isTrue()
            Truth.assertThat(newState.acceptedAction).isEqualTo(Async.Failure<RoomId>(ex))
        }
    }

    @Test
    fun `present - dismisses accepting error state`() = runTest {
        val invitesDataSource = FakeRoomSummaryDataSource().withRoomInvitation()
        val client = FakeMatrixClient(
            sessionId = A_SESSION_ID,
            invitesDataSource = invitesDataSource,
        )
        val room = FakeMatrixRoom()
        val presenter = InviteListPresenter(client)
        val ex = Throwable("Ruh roh!")
        room.givenAcceptInviteResult(Result.failure(ex))
        client.givenGetRoomResult(A_ROOM_ID, room)

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val originalState = awaitItem()
            originalState.eventSink(InviteListEvents.AcceptInvite(originalState.inviteList[0]))

            skipItems(1)

            originalState.eventSink(InviteListEvents.DismissAcceptError)

            val newState = awaitItem()
            Truth.assertThat(newState.acceptedAction).isEqualTo(Async.Uninitialized)
        }
    }

    private suspend fun FakeRoomSummaryDataSource.withRoomInvitation(): FakeRoomSummaryDataSource {
        postRoomSummary(
            listOf(
                RoomSummary.Filled(
                    RoomSummaryDetails(
                        roomId = A_ROOM_ID,
                        name = A_ROOM_NAME,
                        avatarURLString = null,
                        isDirect = false,
                        lastMessage = null,
                        lastMessageTimestamp = null,
                        unreadNotificationCount = 0,
                        inviter = RoomMember(
                            userId = A_USER_ID,
                            displayName = A_USER_NAME,
                            avatarUrl = AN_AVATAR_URL,
                            membership = RoomMembershipState.JOIN,
                            isNameAmbiguous = false,
                            powerLevel = 0,
                            normalizedPowerLevel = 0,
                            isIgnored = false,
                        )
                    )
                )
            )
        )
        return this
    }

    private suspend fun FakeRoomSummaryDataSource.withDirectChatInvitation(): FakeRoomSummaryDataSource {
        postRoomSummary(
            listOf(
                RoomSummary.Filled(
                    RoomSummaryDetails(
                        roomId = A_ROOM_ID,
                        name = A_ROOM_NAME,
                        avatarURLString = null,
                        isDirect = true,
                        lastMessage = null,
                        lastMessageTimestamp = null,
                        unreadNotificationCount = 0,
                        inviter = RoomMember(
                            userId = A_USER_ID,
                            displayName = A_USER_NAME,
                            avatarUrl = AN_AVATAR_URL,
                            membership = RoomMembershipState.JOIN,
                            isNameAmbiguous = false,
                            powerLevel = 0,
                            normalizedPowerLevel = 0,
                            isIgnored = false,
                        )
                    )
                )
            )
        )
        return this
    }
}
