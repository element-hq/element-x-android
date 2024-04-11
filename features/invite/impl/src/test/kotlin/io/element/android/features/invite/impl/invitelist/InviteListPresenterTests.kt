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

package io.element.android.features.invite.impl.invitelist

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.features.invite.api.response.AcceptDeclineInviteState
import io.element.android.features.invite.api.response.anAcceptDeclineInviteState
import io.element.android.features.invite.impl.invitelist.InviteListEvents
import io.element.android.features.invite.impl.invitelist.InviteListPresenter
import io.element.android.features.invite.impl.invitelist.InviteListState
import io.element.android.features.invite.test.FakeSeenInvitesStore
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.matrix.test.room.aRoomSummaryDetails
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.libraries.push.api.notifications.NotificationDrawerManager
import io.element.android.libraries.push.test.notifications.FakeNotificationDrawerManager
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class InviteListPresenterTests {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - starts empty, adds invites when received`() = runTest {
        val roomListService = FakeRoomListService()
        val presenter = createInviteListPresenter(
            FakeMatrixClient(roomListService = roomListService)
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.inviteList).isEmpty()

            roomListService.postInviteRooms(listOf(aRoomSummary()))

            val withInviteState = awaitItem()
            assertThat(withInviteState.inviteList.size).isEqualTo(1)
            assertThat(withInviteState.inviteList[0].roomId).isEqualTo(A_ROOM_ID)
            assertThat(withInviteState.inviteList[0].roomName).isEqualTo(A_ROOM_NAME)
        }
    }

    @Test
    fun `present - uses user ID and avatar for direct invites`() = runTest {
        val roomListService = FakeRoomListService().withDirectChatInvitation()
        val presenter = createInviteListPresenter(
            FakeMatrixClient(roomListService = roomListService)
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val withInviteState = awaitInitialItem()
            assertThat(withInviteState.inviteList.size).isEqualTo(1)
            assertThat(withInviteState.inviteList[0].roomId).isEqualTo(A_ROOM_ID)
            assertThat(withInviteState.inviteList[0].roomAlias).isEqualTo(A_USER_ID.value)
            assertThat(withInviteState.inviteList[0].roomName).isEqualTo(A_ROOM_NAME)
            assertThat(withInviteState.inviteList[0].roomAvatarData).isEqualTo(
                AvatarData(
                    id = A_USER_ID.value,
                    name = A_USER_NAME,
                    url = AN_AVATAR_URL,
                    size = AvatarSize.RoomInviteItem,
                )
            )
            assertThat(withInviteState.inviteList[0].sender).isNull()
        }
    }

    @Test
    fun `present - includes sender details for room invites`() = runTest {
        val roomListService = FakeRoomListService().withRoomInvitation()
        val presenter = createInviteListPresenter(
            FakeMatrixClient(roomListService = roomListService)
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val withInviteState = awaitInitialItem()
            assertThat(withInviteState.inviteList.size).isEqualTo(1)
            assertThat(withInviteState.inviteList[0].sender?.displayName).isEqualTo(A_USER_NAME)
            assertThat(withInviteState.inviteList[0].sender?.userId).isEqualTo(A_USER_ID)
            assertThat(withInviteState.inviteList[0].sender?.avatarData).isEqualTo(
                AvatarData(
                    id = A_USER_ID.value,
                    name = A_USER_NAME,
                    url = AN_AVATAR_URL,
                    size = AvatarSize.InviteSender,
                )
            )
        }
    }

    @Test
    fun `present - stores seen invites when received`() = runTest {
        val roomListService = FakeRoomListService()
        val store = FakeSeenInvitesStore()
        val presenter = createInviteListPresenter(
            FakeMatrixClient(
                roomListService = roomListService,
            ),
            store,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem()

            // When one invite is received, that ID is saved
            roomListService.postInviteRooms(listOf(aRoomSummary()))

            awaitItem()
            assertThat(store.getProvidedRoomIds()).isEqualTo(setOf(A_ROOM_ID))

            // When a second is added, both are saved
            roomListService.postInviteRooms(listOf(aRoomSummary(), aRoomSummary(A_ROOM_ID_2)))

            awaitItem()
            assertThat(store.getProvidedRoomIds()).isEqualTo(setOf(A_ROOM_ID, A_ROOM_ID_2))

            // When they're both dismissed, an empty set is saved
            roomListService.postInviteRooms(listOf())

            awaitItem()
            assertThat(store.getProvidedRoomIds()).isEmpty()
        }
    }

    @Test
    fun `present - marks invite as new if they're unseen`() = runTest {
        val roomListService = FakeRoomListService()
        val store = FakeSeenInvitesStore()
        store.publishRoomIds(setOf(A_ROOM_ID))
        val presenter = createInviteListPresenter(
            FakeMatrixClient(
                roomListService = roomListService,
            ),
            store,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem()

            roomListService.postInviteRooms(listOf(aRoomSummary(), aRoomSummary(A_ROOM_ID_2)))
            skipItems(1)

            val withInviteState = awaitItem()
            assertThat(withInviteState.inviteList.size).isEqualTo(2)
            assertThat(withInviteState.inviteList[0].roomId).isEqualTo(A_ROOM_ID)
            assertThat(withInviteState.inviteList[0].isNew).isFalse()
            assertThat(withInviteState.inviteList[1].roomId).isEqualTo(A_ROOM_ID_2)
            assertThat(withInviteState.inviteList[1].isNew).isTrue()
        }
    }

    private suspend fun FakeRoomListService.withRoomInvitation(): FakeRoomListService {
        postInviteRooms(
            listOf(
                RoomSummary.Filled(
                    aRoomSummaryDetails(
                        roomId = A_ROOM_ID,
                        name = A_ROOM_NAME,
                        avatarUrl = null,
                        isDirect = false,
                        lastMessage = null,
                        inviter = aRoomMember(
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

    private suspend fun FakeRoomListService.withDirectChatInvitation(): FakeRoomListService {
        postInviteRooms(
            listOf(
                RoomSummary.Filled(
                    aRoomSummaryDetails(
                        roomId = A_ROOM_ID,
                        name = A_ROOM_NAME,
                        avatarUrl = null,
                        isDirect = true,
                        lastMessage = null,
                        inviter = aRoomMember(
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

    private fun aRoomSummary(id: RoomId = A_ROOM_ID) = RoomSummary.Filled(
        aRoomSummaryDetails(
            roomId = id,
            name = A_ROOM_NAME,
            avatarUrl = null,
            isDirect = false,
            lastMessage = null,
        )
    )

    private suspend fun TurbineTestContext<InviteListState>.awaitInitialItem(): InviteListState {
        skipItems(1)
        return awaitItem()
    }

    private fun createInviteListPresenter(
        client: MatrixClient,
        seenInvitesStore: SeenInvitesStore = FakeSeenInvitesStore(),
        acceptDeclineInvitePresenter: Presenter<AcceptDeclineInviteState> = Presenter { anAcceptDeclineInviteState() },
    ) = InviteListPresenter(
        client,
        seenInvitesStore,
        acceptDeclineInvitePresenter = acceptDeclineInvitePresenter,
    )
}
