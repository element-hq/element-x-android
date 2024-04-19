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

package io.element.android.features.joinroom.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.features.invite.api.response.AcceptDeclineInviteEvents
import io.element.android.features.invite.api.response.AcceptDeclineInviteState
import io.element.android.features.invite.api.response.anAcceptDeclineInviteState
import io.element.android.features.roomdirectory.api.RoomDescription
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.preview.RoomPreview
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.matrix.ui.model.toInviteSender
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.util.Optional

class JoinRoomPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createJoinRoomPresenter()
        presenter.test {
            awaitItem().also { state ->
                assertThat(state.contentState).isEqualTo(ContentState.Loading(A_ROOM_ID.toRoomIdOrAlias()))
                assertThat(state.joinAuthorisationStatus).isEqualTo(JoinAuthorisationStatus.Unknown)
                assertThat(state.acceptDeclineInviteState).isEqualTo(anAcceptDeclineInviteState())
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `present - when room is joined then content state is filled with his data`() = runTest {
        val roomInfo = aRoomInfo()
        val matrixClient = FakeMatrixClient().apply {
            getRoomInfoFlowLambda = { _ ->
                flowOf(Optional.of(roomInfo))
            }
        }
        val presenter = createJoinRoomPresenter(
            matrixClient = matrixClient
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                val contentState = state.contentState as ContentState.Loaded
                assertThat(contentState.roomId).isEqualTo(A_ROOM_ID)
                assertThat(contentState.name).isEqualTo(roomInfo.name)
                assertThat(contentState.topic).isEqualTo(roomInfo.topic)
                assertThat(contentState.alias).isEqualTo(roomInfo.canonicalAlias)
                assertThat(contentState.numberOfMembers).isEqualTo(roomInfo.activeMembersCount)
                assertThat(contentState.isDirect).isEqualTo(roomInfo.isDirect)
                assertThat(contentState.roomAvatarUrl).isEqualTo(roomInfo.avatarUrl)
            }
        }
    }

    @Test
    fun `present - when room is invited then join authorization is equal to invited`() = runTest {
        val roomInfo = aRoomInfo(currentUserMembership = CurrentUserMembership.INVITED)
        val matrixClient = FakeMatrixClient().apply {
            getRoomInfoFlowLambda = { _ ->
                flowOf(Optional.of(roomInfo))
            }
        }
        val presenter = createJoinRoomPresenter(
            matrixClient = matrixClient
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.joinAuthorisationStatus).isEqualTo(JoinAuthorisationStatus.IsInvited(null))
            }
        }
    }

    @Test
    fun `present - when room is invited then join authorization is equal to invited, and inviter is provided`() = runTest {
        val inviter = aRoomMember(userId = UserId("@bob:example.com"), displayName = "Bob")
        val expectedInviteSender = inviter.toInviteSender()
        val roomInfo = aRoomInfo(
            currentUserMembership = CurrentUserMembership.INVITED,
            inviter = inviter,
        )
        val matrixClient = FakeMatrixClient().apply {
            getRoomInfoFlowLambda = { _ ->
                flowOf(Optional.of(roomInfo))
            }
        }
        val presenter = createJoinRoomPresenter(
            matrixClient = matrixClient
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.joinAuthorisationStatus).isEqualTo(JoinAuthorisationStatus.IsInvited(expectedInviteSender))
            }
        }
    }

    @Test
    fun `present - when room is invited then accept and decline events are sent to acceptDeclinePresenter`() = runTest {
        val eventSinkRecorder = lambdaRecorder { _: AcceptDeclineInviteEvents -> }
        val acceptDeclinePresenter = Presenter {
            anAcceptDeclineInviteState(eventSink = eventSinkRecorder)
        }
        val roomInfo = aRoomInfo(currentUserMembership = CurrentUserMembership.INVITED)
        val matrixClient = FakeMatrixClient().apply {
            getRoomInfoFlowLambda = { _ ->
                flowOf(Optional.of(roomInfo))
            }
        }
        val presenter = createJoinRoomPresenter(
            matrixClient = matrixClient,
            acceptDeclineInvitePresenter = acceptDeclinePresenter
        )
        presenter.test {
            skipItems(1)

            awaitItem().also { state ->
                state.eventSink(JoinRoomEvents.AcceptInvite)
                state.eventSink(JoinRoomEvents.DeclineInvite)

                val inviteData = state.contentState.toInviteData()!!

                assert(eventSinkRecorder)
                    .isCalledExactly(2)
                    .withSequence(
                        listOf(value(AcceptDeclineInviteEvents.AcceptInvite(inviteData))),
                        listOf(value(AcceptDeclineInviteEvents.DeclineInvite(inviteData))),
                    )
            }
        }
    }

    @Test
    fun `present - when room is left and public then join authorization is equal to canJoin`() = runTest {
        val roomInfo = aRoomInfo(currentUserMembership = CurrentUserMembership.LEFT, isPublic = true)
        val matrixClient = FakeMatrixClient().apply {
            getRoomInfoFlowLambda = { _ ->
                flowOf(Optional.of(roomInfo))
            }
        }
        val presenter = createJoinRoomPresenter(
            matrixClient = matrixClient
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.joinAuthorisationStatus).isEqualTo(JoinAuthorisationStatus.CanJoin)
            }
        }
    }

    @Test
    fun `present - when room is left and not public then join authorization is equal to unknown`() = runTest {
        val roomInfo = aRoomInfo(currentUserMembership = CurrentUserMembership.LEFT, isPublic = false)
        val matrixClient = FakeMatrixClient().apply {
            getRoomInfoFlowLambda = { _ ->
                flowOf(Optional.of(roomInfo))
            }
        }
        val presenter = createJoinRoomPresenter(
            matrixClient = matrixClient
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.joinAuthorisationStatus).isEqualTo(JoinAuthorisationStatus.Unknown)
            }
        }
    }

    @Test
    fun `present - when room description is provided and room is not found then content state is filled with data`() = runTest {
        val roomDescription = aRoomDescription()
        val presenter = createJoinRoomPresenter(
            roomDescription = Optional.of(roomDescription)
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                val contentState = state.contentState as ContentState.Loaded
                assertThat(contentState.roomId).isEqualTo(A_ROOM_ID)
                assertThat(contentState.name).isEqualTo(roomDescription.name)
                assertThat(contentState.topic).isEqualTo(roomDescription.topic)
                assertThat(contentState.alias).isEqualTo(roomDescription.alias)
                assertThat(contentState.numberOfMembers).isEqualTo(roomDescription.numberOfMembers)
                assertThat(contentState.isDirect).isFalse()
                assertThat(contentState.roomAvatarUrl).isEqualTo(roomDescription.avatarUrl)
            }
        }
    }

    @Test
    fun `present - when room description join rule is Knock then join authorization is equal to canKnock`() = runTest {
        val roomDescription = aRoomDescription(joinRule = RoomDescription.JoinRule.KNOCK)
        val presenter = createJoinRoomPresenter(
            roomDescription = Optional.of(roomDescription)
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.joinAuthorisationStatus).isEqualTo(JoinAuthorisationStatus.CanKnock)
            }
        }
    }

    @Test
    fun `present - when room description join rule is Public then join authorization is equal to canJoin`() = runTest {
        val roomDescription = aRoomDescription(joinRule = RoomDescription.JoinRule.PUBLIC)
        val presenter = createJoinRoomPresenter(
            roomDescription = Optional.of(roomDescription)
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.joinAuthorisationStatus).isEqualTo(JoinAuthorisationStatus.CanJoin)
            }
        }
    }

    @Test
    fun `present - when room description join rule is Unknown then join authorization is equal to unknown`() = runTest {
        val roomDescription = aRoomDescription(joinRule = RoomDescription.JoinRule.UNKNOWN)
        val presenter = createJoinRoomPresenter(
            roomDescription = Optional.of(roomDescription)
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.joinAuthorisationStatus).isEqualTo(JoinAuthorisationStatus.Unknown)
            }
        }
    }

    @Test
    fun `present - when room is not known RoomPreview is loaded`() = runTest {
        val client = FakeMatrixClient(
            getRoomPreviewResult = {
                Result.success(
                    RoomPreview(
                        roomId = A_ROOM_ID,
                        canonicalAlias = RoomAlias("#alias:matrix.org"),
                        name = "Room name",
                        topic = "Room topic",
                        avatarUrl = "avatarUrl",
                        numberOfJoinedMembers = 2,
                        roomType = null,
                        isHistoryWorldReadable = false,
                        isJoined = false,
                        isInvited = false,
                        isPublic = true,
                        canKnock = false,
                    )
                )
            }
        )
        val presenter = createJoinRoomPresenter(
            matrixClient = client
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.contentState).isEqualTo(
                    ContentState.Loaded(
                        roomId = A_ROOM_ID,
                        name = "Room name",
                        topic = "Room topic",
                        alias = RoomAlias("#alias:matrix.org"),
                        numberOfMembers = 2,
                        isDirect = false,
                        roomAvatarUrl = "avatarUrl",
                        joinAuthorisationStatus = JoinAuthorisationStatus.CanJoin
                    )
                )
            }
        }
    }

    @Test
    fun `present - when room is not known RoomPreview is loaded with error`() = runTest {
        val client = FakeMatrixClient(
            getRoomPreviewResult = {
                Result.failure(AN_EXCEPTION)
            }
        )
        val presenter = createJoinRoomPresenter(
            matrixClient = client
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.contentState).isEqualTo(
                    ContentState.Failure(
                        roomIdOrAlias = A_ROOM_ID.toRoomIdOrAlias(),
                        error = AN_EXCEPTION
                    )
                )
                state.eventSink(JoinRoomEvents.RetryFetchingContent)
            }
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.contentState).isEqualTo(
                    ContentState.Loading(A_ROOM_ID.toRoomIdOrAlias())
                )
            }
            awaitItem().also { state ->
                assertThat(state.contentState).isEqualTo(
                    ContentState.Failure(
                        roomIdOrAlias = A_ROOM_ID.toRoomIdOrAlias(),
                        error = AN_EXCEPTION
                    )
                )
            }
        }
    }

    @Test
    fun `present - when room is not known RoomPreview is loaded with error 403`() = runTest {
        val client = FakeMatrixClient(
            getRoomPreviewResult = {
                Result.failure(Exception("403"))
            }
        )
        val presenter = createJoinRoomPresenter(
            matrixClient = client
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.contentState).isEqualTo(
                    ContentState.UnknownRoom(
                        roomIdOrAlias = A_ROOM_ID.toRoomIdOrAlias(),
                    )
                )
            }
        }
    }

    private fun createJoinRoomPresenter(
        roomId: RoomId = A_ROOM_ID,
        roomDescription: Optional<RoomDescription> = Optional.empty(),
        matrixClient: MatrixClient = FakeMatrixClient(),
        acceptDeclineInvitePresenter: Presenter<AcceptDeclineInviteState> = Presenter { anAcceptDeclineInviteState() }
    ): JoinRoomPresenter {
        return JoinRoomPresenter(
            roomId = roomId,
            roomIdOrAlias = roomId.toRoomIdOrAlias(),
            roomDescription = roomDescription,
            matrixClient = matrixClient,
            acceptDeclineInvitePresenter = acceptDeclineInvitePresenter
        )
    }

    private fun aRoomDescription(
        roomId: RoomId = A_ROOM_ID,
        name: String? = A_ROOM_NAME,
        topic: String? = "A room about something",
        alias: RoomAlias? = RoomAlias("#alias:matrix.org"),
        avatarUrl: String? = null,
        joinRule: RoomDescription.JoinRule = RoomDescription.JoinRule.UNKNOWN,
        numberOfMembers: Long = 2L
    ): RoomDescription {
        return RoomDescription(
            roomId = roomId,
            name = name,
            topic = topic,
            alias = alias,
            avatarUrl = avatarUrl,
            joinRule = joinRule,
            numberOfMembers = numberOfMembers
        )
    }
}
