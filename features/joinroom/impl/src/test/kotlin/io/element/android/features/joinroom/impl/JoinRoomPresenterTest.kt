/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.joinroom.impl

import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.features.invite.api.response.AcceptDeclineInviteEvents
import io.element.android.features.invite.api.response.AcceptDeclineInviteState
import io.element.android.features.invite.api.response.anAcceptDeclineInviteState
import io.element.android.features.joinroom.impl.di.CancelKnockRoom
import io.element.android.features.joinroom.impl.di.KnockRoom
import io.element.android.features.roomdirectory.api.RoomDescription
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomType
import io.element.android.libraries.matrix.api.room.preview.RoomPreview
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_SERVER_LIST
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.matrix.test.room.aRoomSummary
import io.element.android.libraries.matrix.test.room.join.FakeJoinRoom
import io.element.android.libraries.matrix.ui.model.toInviteSender
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.any
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
                assertThat(state.cancelKnockAction).isEqualTo(AsyncAction.Uninitialized)
                assertThat(state.knockAction).isEqualTo(AsyncAction.Uninitialized)
                assertThat(state.applicationName).isEqualTo("AppName")
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `present - when room is joined then content state is filled with his data`() = runTest {
        val roomSummary = aRoomSummary()
        val matrixClient = FakeMatrixClient().apply {
            getRoomSummaryFlowLambda = { _ ->
                flowOf(Optional.of(roomSummary))
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
                assertThat(contentState.name).isEqualTo(roomSummary.info.name)
                assertThat(contentState.topic).isEqualTo(roomSummary.info.topic)
                assertThat(contentState.alias).isEqualTo(roomSummary.info.canonicalAlias)
                assertThat(contentState.numberOfMembers).isEqualTo(roomSummary.info.activeMembersCount)
                assertThat(contentState.isDm).isEqualTo(roomSummary.info.isDirect)
                assertThat(contentState.roomAvatarUrl).isEqualTo(roomSummary.info.avatarUrl)
            }
        }
    }

    @Test
    fun `present - when room is invited then join authorization is equal to invited`() = runTest {
        val roomSummary = aRoomSummary(currentUserMembership = CurrentUserMembership.INVITED)
        val matrixClient = FakeMatrixClient().apply {
            getRoomSummaryFlowLambda = { _ ->
                flowOf(Optional.of(roomSummary))
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
    fun `present - when room is invited then join authorization is equal to invited, an inviter is provided`() = runTest {
        val inviter = aRoomMember(userId = UserId("@bob:example.com"), displayName = "Bob")
        val expectedInviteSender = inviter.toInviteSender()
        val roomSummary = aRoomSummary(
            currentUserMembership = CurrentUserMembership.INVITED,
            inviter = inviter,
        )
        val matrixClient = FakeMatrixClient().apply {
            getRoomSummaryFlowLambda = { _ ->
                flowOf(Optional.of(roomSummary))
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
        val roomSummary = aRoomSummary(currentUserMembership = CurrentUserMembership.INVITED)
        val matrixClient = FakeMatrixClient().apply {
            getRoomSummaryFlowLambda = { _ ->
                flowOf(Optional.of(roomSummary))
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
    fun `present - when room is joined with success, all the parameters are provided`() = runTest {
        val aTrigger = JoinedRoom.Trigger.MobilePermalink
        val joinRoomLambda = lambdaRecorder { _: RoomIdOrAlias, _: List<String>, _: JoinedRoom.Trigger ->
            Result.success(Unit)
        }
        val presenter = createJoinRoomPresenter(
            trigger = aTrigger,
            serverNames = A_SERVER_LIST,
            joinRoomLambda = joinRoomLambda,
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                state.eventSink(JoinRoomEvents.JoinRoom)
            }
            awaitItem().also { state ->
                assertThat(state.joinAction).isEqualTo(AsyncAction.Loading)
            }
            awaitItem().also { state ->
                assertThat(state.joinAction).isEqualTo(AsyncAction.Success(Unit))
            }
            joinRoomLambda.assertions()
                .isCalledOnce()
                .with(value(A_ROOM_ID.toRoomIdOrAlias()), value(A_SERVER_LIST), value(aTrigger))
        }
    }

    @Test
    fun `present - when room is joined with error, it is possible to clear the error`() = runTest {
        val presenter = createJoinRoomPresenter(
            joinRoomLambda = { _, _, _ ->
                Result.failure(AN_EXCEPTION)
            },
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                state.eventSink(JoinRoomEvents.JoinRoom)
            }
            awaitItem().also { state ->
                assertThat(state.joinAction).isEqualTo(AsyncAction.Loading)
            }
            awaitItem().also { state ->
                assertThat(state.joinAction).isEqualTo(AsyncAction.Failure(AN_EXCEPTION))
                state.eventSink(JoinRoomEvents.ClearActionStates)
            }
            awaitItem().also { state ->
                assertThat(state.joinAction).isEqualTo(AsyncAction.Uninitialized)
            }
        }
    }

    @Test
    fun `present - when room is left and public then join authorization is equal to canJoin`() = runTest {
        val roomSummary = aRoomSummary(currentUserMembership = CurrentUserMembership.LEFT, isPublic = true)
        val matrixClient = FakeMatrixClient().apply {
            getRoomSummaryFlowLambda = { _ ->
                flowOf(Optional.of(roomSummary))
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
        val roomSummary = aRoomSummary(currentUserMembership = CurrentUserMembership.LEFT, isPublic = false)
        val matrixClient = FakeMatrixClient().apply {
            getRoomSummaryFlowLambda = { _ ->
                flowOf(Optional.of(roomSummary))
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
                assertThat(contentState.isDm).isFalse()
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
    fun `present - emit knock room event`() = runTest {
        val knockMessage = "Knock message"
        val knockRoomSuccess = lambdaRecorder { _: RoomIdOrAlias, _: String, _: List<String> ->
            Result.success(Unit)
        }
        val knockRoomFailure = lambdaRecorder { roomIdOrAlias: RoomIdOrAlias, _: String, _: List<String> ->
            Result.failure<Unit>(RuntimeException("Failed to knock room $roomIdOrAlias"))
        }
        val fakeKnockRoom = FakeKnockRoom(knockRoomSuccess)
        val presenter = createJoinRoomPresenter(knockRoom = fakeKnockRoom)
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                state.eventSink(JoinRoomEvents.UpdateKnockMessage(knockMessage))
            }
            awaitItem().also { state ->
                state.eventSink(JoinRoomEvents.KnockRoom)
            }

            assertThat(awaitItem().knockAction).isEqualTo(AsyncAction.Loading)
            awaitItem().also { state ->
                assertThat(state.knockAction).isEqualTo(AsyncAction.Success(Unit))
                fakeKnockRoom.lambda = knockRoomFailure
                state.eventSink(JoinRoomEvents.KnockRoom)
            }

            assertThat(awaitItem().knockAction).isEqualTo(AsyncAction.Loading)
            awaitItem().also { state ->
                assertThat(state.knockAction).isInstanceOf(AsyncAction.Failure::class.java)
            }
        }
        assert(knockRoomSuccess)
            .isCalledOnce()
            .with(value(A_ROOM_ID.toRoomIdOrAlias()), value(knockMessage), any())
        assert(knockRoomFailure)
            .isCalledOnce()
            .with(value(A_ROOM_ID.toRoomIdOrAlias()), value(knockMessage), any())
    }

    @Test
    fun `present - emit cancel knock room event`() = runTest {
        val cancelKnockRoomSuccess = lambdaRecorder { _: RoomId ->
            Result.success(Unit)
        }
        val cancelKnockRoomFailure = lambdaRecorder { roomId: RoomId ->
            Result.failure<Unit>(RuntimeException("Failed to knock room $roomId"))
        }
        val cancelKnockRoom = FakeCancelKnockRoom(cancelKnockRoomSuccess)
        val presenter = createJoinRoomPresenter(cancelKnockRoom = cancelKnockRoom)
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                state.eventSink(JoinRoomEvents.CancelKnock(true))
            }
            awaitItem().also { state ->
                assertThat(state.cancelKnockAction).isEqualTo(AsyncAction.ConfirmingNoParams)
                state.eventSink(JoinRoomEvents.CancelKnock(false))
            }
            assertThat(awaitItem().cancelKnockAction).isEqualTo(AsyncAction.Loading)
            awaitItem().also { state ->
                assertThat(state.cancelKnockAction).isEqualTo(AsyncAction.Success(Unit))
                cancelKnockRoom.lambda = cancelKnockRoomFailure
                state.eventSink(JoinRoomEvents.CancelKnock(false))
            }
            assertThat(awaitItem().cancelKnockAction).isEqualTo(AsyncAction.Loading)
            awaitItem().also { state ->
                assertThat(state.cancelKnockAction).isInstanceOf(AsyncAction.Failure::class.java)
            }
        }
        assert(cancelKnockRoomFailure)
            .isCalledOnce()
            .with(value(A_ROOM_ID))
        assert(cancelKnockRoomSuccess)
            .isCalledOnce()
            .with(value(A_ROOM_ID))
    }

    @Test
    fun `present - when room is not known RoomPreview is loaded`() = runTest {
        val client = FakeMatrixClient(
            getRoomPreviewResult = { _, _ ->
                Result.success(
                    RoomPreview(
                        roomId = A_ROOM_ID,
                        canonicalAlias = RoomAlias("#alias:matrix.org"),
                        name = "Room name",
                        topic = "Room topic",
                        avatarUrl = "avatarUrl",
                        numberOfJoinedMembers = 2,
                        roomType = RoomType.Room,
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
                        isDm = false,
                        roomType = RoomType.Room,
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
            getRoomPreviewResult = { _, _ ->
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
            getRoomPreviewResult = { _, _ ->
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
        serverNames: List<String> = emptyList(),
        trigger: JoinedRoom.Trigger = JoinedRoom.Trigger.Invite,
        matrixClient: MatrixClient = FakeMatrixClient(),
        joinRoomLambda: (RoomIdOrAlias, List<String>, JoinedRoom.Trigger) -> Result<Unit> = { _, _, _ ->
            Result.success(Unit)
        },
        knockRoom: KnockRoom = FakeKnockRoom(),
        cancelKnockRoom: CancelKnockRoom = FakeCancelKnockRoom(),
        buildMeta: BuildMeta = aBuildMeta(applicationName = "AppName"),
        acceptDeclineInvitePresenter: Presenter<AcceptDeclineInviteState> = Presenter { anAcceptDeclineInviteState() }
    ): JoinRoomPresenter {
        return JoinRoomPresenter(
            roomId = roomId,
            roomIdOrAlias = roomId.toRoomIdOrAlias(),
            roomDescription = roomDescription,
            serverNames = serverNames,
            trigger = trigger,
            matrixClient = matrixClient,
            joinRoom = FakeJoinRoom(joinRoomLambda),
            knockRoom = knockRoom,
            cancelKnockRoom = cancelKnockRoom,
            buildMeta = buildMeta,
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
