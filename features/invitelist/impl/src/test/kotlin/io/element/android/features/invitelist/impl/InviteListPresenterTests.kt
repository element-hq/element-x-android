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
import io.element.android.libraries.designsystem.components.avatar.AvatarData
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
        val invitesDataSource = FakeRoomSummaryDataSource()
        invitesDataSource.postRoomSummary(
            listOf(
                RoomSummary.Filled(
                    RoomSummaryDetails(
                        roomId = A_ROOM_ID,
                        name = A_USER_NAME,
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
            Truth.assertThat(withInviteState.inviteList[0].roomName).isEqualTo(A_USER_NAME)
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
        val invitesDataSource = FakeRoomSummaryDataSource()
        invitesDataSource.postRoomSummary(
            listOf(
                RoomSummary.Filled(
                    RoomSummaryDetails(
                        roomId = A_ROOM_ID,
                        name = A_USER_NAME,
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
}
