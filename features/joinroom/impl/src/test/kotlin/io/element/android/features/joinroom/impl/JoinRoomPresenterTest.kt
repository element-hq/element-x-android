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
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.aRoomInfo
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
                assertThat(state.contentState).isInstanceOf(AsyncData.Uninitialized::class.java)
                assertThat(state.joinAuthorisationStatus).isEqualTo(JoinAuthorisationStatus.Unknown)
                assertThat(state.acceptDeclineInviteState).isEqualTo(anAcceptDeclineInviteState())
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
                assertThat(state.contentState).isInstanceOf(AsyncData.Success::class.java)
                val contentState = state.contentState.dataOrNull()!!
                assertThat(contentState.roomId).isEqualTo(A_ROOM_ID)
                assertThat(contentState.name).isEqualTo(roomInfo.name)
                assertThat(contentState.description).isEqualTo(roomInfo.topic)
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
                assertThat(state.joinAuthorisationStatus).isEqualTo(JoinAuthorisationStatus.IsInvited)
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
                assertThat(state.contentState).isInstanceOf(AsyncData.Success::class.java)
                val contentState = state.contentState.dataOrNull()!!
                assertThat(contentState.roomId).isEqualTo(A_ROOM_ID)
                assertThat(contentState.name).isEqualTo(roomDescription.name)
                assertThat(contentState.description).isEqualTo(roomDescription.description)
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

    private fun createJoinRoomPresenter(
        roomId: RoomId = A_ROOM_ID,
        roomDescription: Optional<RoomDescription> = Optional.empty(),
        matrixClient: MatrixClient = FakeMatrixClient(),
        acceptDeclineInvitePresenter: Presenter<AcceptDeclineInviteState> = Presenter { anAcceptDeclineInviteState() }
    ): JoinRoomPresenter {
        return JoinRoomPresenter(
            roomId = roomId,
            roomDescription = roomDescription,
            matrixClient = matrixClient,
            acceptDeclineInvitePresenter = acceptDeclineInvitePresenter
        )
    }

    private fun aRoomDescription(
        roomId: RoomId = A_ROOM_ID,
        name: String = A_ROOM_NAME,
        description: String = "A room about something",
        avatarUrl: String? = null,
        joinRule: RoomDescription.JoinRule = RoomDescription.JoinRule.UNKNOWN,
        numberOfMembers: Long = 2L
    ): RoomDescription {
        return RoomDescription(
            roomId = roomId,
            name = name,
            description = description,
            avatarUrl = avatarUrl,
            joinRule = joinRule,
            numberOfMembers = numberOfMembers
        )
    }
}
