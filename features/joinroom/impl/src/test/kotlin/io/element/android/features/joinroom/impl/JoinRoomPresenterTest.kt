/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.joinroom.impl

import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.features.invite.api.InviteData
import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteEvents
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteState
import io.element.android.features.invite.api.acceptdecline.anAcceptDeclineInviteState
import io.element.android.features.invite.api.toInviteData
import io.element.android.features.invite.test.InMemorySeenInvitesStore
import io.element.android.features.joinroom.impl.di.CancelKnockRoom
import io.element.android.features.joinroom.impl.di.ForgetRoom
import io.element.android.features.joinroom.impl.di.KnockRoom
import io.element.android.features.roomdirectory.api.RoomDescription
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.exception.ClientException
import io.element.android.libraries.matrix.api.exception.ErrorKind
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomMembershipDetails
import io.element.android.libraries.matrix.api.room.join.JoinRoom
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_SERVER_LIST
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.matrix.test.room.aRoomPreview
import io.element.android.libraries.matrix.test.room.aRoomPreviewInfo
import io.element.android.libraries.matrix.test.room.join.FakeJoinRoom
import io.element.android.libraries.matrix.test.spaces.FakeSpaceRoomList
import io.element.android.libraries.matrix.test.spaces.FakeSpaceService
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.model.InviteSender
import io.element.android.libraries.matrix.ui.model.toInviteSender
import io.element.android.libraries.previewutils.room.aSpaceRoom
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.any
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.util.Optional

@Suppress("LargeClass")
class JoinRoomPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createJoinRoomPresenter()
        presenter.test {
            awaitItem().also { state ->
                assertThat(state.contentState).isEqualTo(ContentState.Loading)
                assertThat(state.acceptDeclineInviteState).isEqualTo(anAcceptDeclineInviteState())
                assertThat(state.cancelKnockAction).isEqualTo(AsyncAction.Uninitialized)
                assertThat(state.knockAction).isEqualTo(AsyncAction.Uninitialized)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `present - when room is joined then content state is filled with his data`() = runTest {
        val roomInfo = aRoomInfo()
        val matrixClient = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ -> Result.failure(AN_EXCEPTION) },
            spaceService = FakeSpaceService(
                spaceRoomListResult = { FakeSpaceRoomList() },
            ),
        ).apply {
            getRoomInfoFlowLambda = { _ ->
                flowOf(Optional.of(roomInfo))
            }
        }
        val presenter = createJoinRoomPresenter(
            matrixClient = matrixClient
        )
        presenter.test {
            skipItems(2)
            awaitItem().also { state ->
                val contentState = state.contentState as ContentState.Loaded
                assertThat(contentState.roomId).isEqualTo(A_ROOM_ID)
                assertThat(contentState.name).isEqualTo(roomInfo.name)
                assertThat(contentState.topic).isEqualTo(roomInfo.topic)
                assertThat(contentState.alias).isEqualTo(roomInfo.canonicalAlias)
                assertThat(contentState.numberOfMembers).isEqualTo(roomInfo.joinedMembersCount)
                assertThat(contentState.details).isEqualTo(aLoadedDetailsRoom(isDm = roomInfo.isDirect))
                assertThat(contentState.roomAvatarUrl).isEqualTo(roomInfo.avatarUrl)
            }
        }
    }

    @Test
    fun `present - when room is invited then join authorization is equal to invited`() = runTest {
        val roomInfo = aRoomInfo(currentUserMembership = CurrentUserMembership.INVITED)
        val matrixClient = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ -> Result.failure(AN_EXCEPTION) },
            spaceService = FakeSpaceService(
                spaceRoomListResult = { FakeSpaceRoomList() },
            ),
        ).apply {
            getRoomInfoFlowLambda = { _ ->
                flowOf(Optional.of(roomInfo))
            }
        }
        val seenInvitesStore = InMemorySeenInvitesStore()
        val presenter = createJoinRoomPresenter(
            matrixClient = matrixClient,
            seenInvitesStore = seenInvitesStore,
        )
        val inviteData = roomInfo.toInviteData()
        assertThat(seenInvitesStore.seenRoomIds().first()).isEmpty()
        presenter.test {
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.joinAuthorisationStatus).isEqualTo(JoinAuthorisationStatus.IsInvited(inviteData, null))
            }
            // Check that the roomId is stored in the seen invites store
            assertThat(seenInvitesStore.seenRoomIds().first()).containsExactly(roomInfo.id)
        }
    }

    @Test
    fun `present - when room is invited then join authorization is equal to invited, an inviter is provided`() = runTest {
        val inviter = aRoomMember(userId = A_USER_ID_2, displayName = "Bob")
        val expectedInviteSender = inviter.toInviteSender()
        val roomInfo = aRoomInfo(
            currentUserMembership = CurrentUserMembership.INVITED,
            joinedMembersCount = 5,
            inviter = inviter,
        )
        val inviteData = roomInfo.toInviteData()
        val matrixClient = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ ->
                Result.success(
                    aRoomPreview(
                        info = aRoomPreviewInfo(
                            numberOfJoinedMembers = 5,
                        ),
                        roomMembershipDetails = {
                            Result.success(aRoomMembershipDetails())
                        },
                    )
                )
            },
            spaceService = FakeSpaceService(
                spaceRoomListResult = { FakeSpaceRoomList() },
            ),
        ).apply {
            getRoomInfoFlowLambda = { _ ->
                flowOf(Optional.of(roomInfo))
            }
        }
        val presenter = createJoinRoomPresenter(
            matrixClient = matrixClient
        )
        presenter.test {
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.joinAuthorisationStatus).isEqualTo(JoinAuthorisationStatus.IsInvited(inviteData, expectedInviteSender))
                assertThat((state.contentState as ContentState.Loaded).numberOfMembers).isEqualTo(5)
            }
        }
    }

    @Test
    fun `present - when space is invited then join authorization is equal to invited, an inviter is provided`() = runTest {
        val inviter = aRoomMember(userId = A_USER_ID_2, displayName = "Bob")
        val expectedInviteSender = inviter.toInviteSender()
        val spaceHero = aMatrixUser()
        val roomInfo = aRoomInfo(
            isSpace = true,
            currentUserMembership = CurrentUserMembership.INVITED,
            joinedMembersCount = 5,
            inviter = inviter,
            heroes = listOf(spaceHero),
        )
        val inviteData = roomInfo.toInviteData()
        val matrixClient = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ ->
                Result.success(
                    aRoomPreview(
                        info = aRoomPreviewInfo(
                            numberOfJoinedMembers = 5,
                        ),
                        roomMembershipDetails = {
                            Result.success(aRoomMembershipDetails())
                        },
                    )
                )
            },
            spaceService = FakeSpaceService(
                spaceRoomListResult = {
                    FakeSpaceRoomList(
                        initialSpaceFlowValue = aSpaceRoom(
                            childrenCount = 3,
                        )
                    )
                },
            ),
        ).apply {
            getRoomInfoFlowLambda = { _ ->
                flowOf(Optional.of(roomInfo))
            }
        }
        val presenter = createJoinRoomPresenter(
            matrixClient = matrixClient
        )
        presenter.test {
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.joinAuthorisationStatus).isEqualTo(JoinAuthorisationStatus.IsInvited(inviteData, expectedInviteSender))
                assertThat((state.contentState as ContentState.Loaded).numberOfMembers).isEqualTo(5)
                // Space details are provided
                assertThat(state.contentState.details).isEqualTo(
                    LoadedDetails.Space(
                        childrenCount = 3,
                        heroes = persistentListOf(spaceHero),
                    )
                )
            }
        }
    }

    @Test
    fun `present - space is invited - no room info`() = runTest {
        val spaceHero = aMatrixUser()
        val spaceRoom = aSpaceRoom(
            childrenCount = 3,
            heroes = listOf(spaceHero),
        )
        val matrixClient = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ ->
                Result.failure(Exception("Error"))
            },
            spaceService = FakeSpaceService(
                spaceRoomListResult = {
                    FakeSpaceRoomList(
                        initialSpaceFlowValue = spaceRoom,
                    )
                },
            ),
        ).apply {
            getRoomInfoFlowLambda = { _ ->
                flowOf(Optional.ofNullable(null))
            }
        }
        val presenter = createJoinRoomPresenter(
            matrixClient = matrixClient
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                // Space details are provided
                assertThat((state.contentState as ContentState.Loaded).details).isEqualTo(
                    LoadedDetails.Space(
                        childrenCount = 3,
                        heroes = persistentListOf(spaceHero),
                    )
                )
            }
        }
    }

    @Test
    fun `present - space is invited - no room info - space room state set`() = runTest {
        val spaceRoom = aSpaceRoom(
            state = CurrentUserMembership.INVITED,
        )
        val matrixClient = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ ->
                Result.failure(Exception("Error"))
            },
            spaceService = FakeSpaceService(
                spaceRoomListResult = {
                    FakeSpaceRoomList(
                        initialSpaceFlowValue = spaceRoom,
                    )
                },
            ),
        ).apply {
            getRoomInfoFlowLambda = { _ ->
                flowOf(Optional.ofNullable(null))
            }
        }
        val presenter = createJoinRoomPresenter(
            matrixClient = matrixClient
        )
        presenter.test {
            awaitItem().also { state ->
                // Space details are provided
                assertThat(state.contentState).isInstanceOf(ContentState.Loading::class.java)
            }
        }
    }

    @Test
    fun `present - when room is invited read the number of member from the room preview`() = runTest {
        val roomInfo = aRoomInfo(
            currentUserMembership = CurrentUserMembership.INVITED,
            // It seems that the SDK does not provide this value.
            joinedMembersCount = 0,
        )
        val matrixClient = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ ->
                Result.success(
                    aRoomPreview(
                        info = aRoomPreviewInfo(
                            numberOfJoinedMembers = 10,
                        ),
                        roomMembershipDetails = {
                            Result.success(aRoomMembershipDetails())
                        },
                    )
                )
            },
            spaceService = FakeSpaceService(
                spaceRoomListResult = { FakeSpaceRoomList() },
            ),
        ).apply {
            getRoomInfoFlowLambda = { _ ->
                flowOf(Optional.of(roomInfo))
            }
        }
        val presenter = createJoinRoomPresenter(
            matrixClient = matrixClient
        )
        presenter.test {
            skipItems(2)
            awaitItem().also { state ->
                assertThat((state.contentState as ContentState.Loaded).numberOfMembers).isEqualTo(10)
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
        val matrixClient = FakeMatrixClient(
            spaceService = FakeSpaceService(
                spaceRoomListResult = { FakeSpaceRoomList() },
            ),
        ).apply {
            getRoomInfoFlowLambda = { _ ->
                flowOf(Optional.of(roomInfo))
            }
        }
        val inviteData = roomInfo.toInviteData()
        val presenter = createJoinRoomPresenter(
            matrixClient = matrixClient,
            acceptDeclineInvitePresenter = acceptDeclinePresenter
        )
        presenter.test {
            skipItems(1)

            awaitItem().also { state ->
                state.eventSink(JoinRoomEvents.AcceptInvite(inviteData))
                state.eventSink(JoinRoomEvents.DeclineInvite(inviteData, false))

                assert(eventSinkRecorder)
                    .isCalledExactly(2)
                    .withSequence(
                        listOf(value(AcceptDeclineInviteEvents.AcceptInvite(inviteData))),
                        listOf(value(AcceptDeclineInviteEvents.DeclineInvite(inviteData, blockUser = false, shouldConfirm = true))),
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
        val matrixClient = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ -> Result.failure(AN_EXCEPTION) },
            spaceService = FakeSpaceService(
                spaceRoomListResult = { FakeSpaceRoomList() },
            ),
        )
        val presenter = createJoinRoomPresenter(
            matrixClient = matrixClient,
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
        val matrixClient = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ -> Result.failure(AN_EXCEPTION) },
            spaceService = FakeSpaceService(
                spaceRoomListResult = { FakeSpaceRoomList() },
            ),
        )
        val presenter = createJoinRoomPresenter(
            matrixClient = matrixClient,
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
    fun `present - when room is joined with unauthorized error, then the authorisation status is unauthorized`() = runTest {
        val roomDescription = aRoomDescription()
        val presenter = createJoinRoomPresenter(
            roomDescription = Optional.of(roomDescription),
            joinRoomLambda = { _, _, _ ->
                Result.failure(JoinRoom.Failures.UnauthorizedJoin)
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
                assertThat(state.joinAction).isEqualTo(AsyncAction.Failure(JoinRoom.Failures.UnauthorizedJoin))
                assertThat(state.joinAuthorisationStatus).isEqualTo(JoinAuthorisationStatus.Unauthorized)
            }
        }
    }

    @Test
    fun `present - when room is banned, then join authorization is equal to IsBanned`() = runTest {
        val roomInfo = aRoomInfo(currentUserMembership = CurrentUserMembership.BANNED, joinRule = JoinRule.Public)
        val matrixClient = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ ->
                Result.success(
                    aRoomPreview(
                        info = aRoomPreviewInfo(
                            roomId = A_ROOM_ID,
                            joinRule = JoinRule.Public,
                            currentUserMembership = CurrentUserMembership.BANNED,
                        ),
                        roomMembershipDetails = {
                            Result.success(aRoomMembershipDetails())
                        }
                    )
                )
            },
            spaceService = FakeSpaceService(
                spaceRoomListResult = { FakeSpaceRoomList() },
            ),
        ).apply {
            getRoomInfoFlowLambda = { _ ->
                flowOf(Optional.of(roomInfo))
            }
        }
        val presenter = createJoinRoomPresenter(
            matrixClient = matrixClient
        )
        presenter.test {
            // Skip initial state
            skipItems(1)

            // Advance until the room info is loaded and the presenter recomposes. The room preview info still needs to be loaded async.
            skipItems(1)

            // Now we should have the room info
            awaitItem().also { state ->
                assertThat(state.joinAuthorisationStatus).isInstanceOf(JoinAuthorisationStatus.IsBanned::class.java)
            }
        }
    }

    @Test
    fun `present - when room is left and public then join authorization is equal to canJoin`() = runTest {
        val roomInfo = aRoomInfo(currentUserMembership = CurrentUserMembership.LEFT, joinRule = JoinRule.Public)
        val matrixClient = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ -> Result.failure(AN_EXCEPTION) },
            spaceService = FakeSpaceService(
                spaceRoomListResult = { FakeSpaceRoomList() },
            ),
        ).apply {
            getRoomInfoFlowLambda = { _ ->
                flowOf(Optional.of(roomInfo))
            }
        }
        val presenter = createJoinRoomPresenter(
            matrixClient = matrixClient
        )
        presenter.test {
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.joinAuthorisationStatus).isEqualTo(JoinAuthorisationStatus.CanJoin)
            }
        }
    }

    @Test
    fun `present - when room is left and join rule null then join authorization is equal to Unknown`() = runTest {
        val roomInfo = aRoomInfo(currentUserMembership = CurrentUserMembership.LEFT, joinRule = null)
        val matrixClient = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ -> Result.failure(AN_EXCEPTION) },
            spaceService = FakeSpaceService(
                spaceRoomListResult = { FakeSpaceRoomList() },
            ),
        ).apply {
            getRoomInfoFlowLambda = { _ ->
                flowOf(Optional.of(roomInfo))
            }
        }
        val presenter = createJoinRoomPresenter(
            matrixClient = matrixClient
        )
        presenter.test {
            skipItems(2)
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
                assertThat(contentState.details).isEqualTo(aLoadedDetailsRoom(isDm = false))
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
    fun `present - when room preview join rule is Private then join authorization is equal to NeedInvite`() = runTest {
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
        val matrixClient = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ -> Result.failure(AN_EXCEPTION) },
            spaceService = FakeSpaceService(
                spaceRoomListResult = { FakeSpaceRoomList() },
            ),
        )
        val presenter = createJoinRoomPresenter(
            matrixClient = matrixClient,
            knockRoom = fakeKnockRoom,
        )
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
        val matrixClient = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ -> Result.failure(AN_EXCEPTION) },
            spaceService = FakeSpaceService(
                spaceRoomListResult = { FakeSpaceRoomList() },
            ),
        )
        val presenter = createJoinRoomPresenter(
            matrixClient = matrixClient,
            cancelKnockRoom = cancelKnockRoom,
        )
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
    fun `present - emit forget room event`() = runTest {
        val forgetRoomSuccess = lambdaRecorder { _: RoomId ->
            Result.success(Unit)
        }
        val forgetRoomFailure = lambdaRecorder { _: RoomId ->
            Result.failure<Unit>(RuntimeException("Failed to forget room"))
        }
        val fakeForgetRoom = FakeForgetRoom(forgetRoomSuccess)
        val matrixClient = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ -> Result.failure(AN_EXCEPTION) },
            spaceService = FakeSpaceService(
                spaceRoomListResult = { FakeSpaceRoomList() },
            ),
        )
        val presenter = createJoinRoomPresenter(
            matrixClient = matrixClient,
            forgetRoom = fakeForgetRoom,
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                state.eventSink(JoinRoomEvents.ForgetRoom)
            }

            assertThat(awaitItem().forgetAction).isEqualTo(AsyncAction.Loading)
            awaitItem().also { state ->
                assertThat(state.forgetAction).isEqualTo(AsyncAction.Success(Unit))
                fakeForgetRoom.lambda = forgetRoomFailure
                state.eventSink(JoinRoomEvents.ForgetRoom)
            }

            assertThat(awaitItem().forgetAction).isEqualTo(AsyncAction.Loading)
            awaitItem().also { state ->
                assertThat(state.forgetAction).isInstanceOf(AsyncAction.Failure::class.java)
            }
        }
        assert(forgetRoomFailure)
            .isCalledOnce()
            .with(value(A_ROOM_ID))
        assert(forgetRoomSuccess)
            .isCalledOnce()
            .with(value(A_ROOM_ID))
    }

    @Test
    fun `present - when room is not known RoomPreview is loaded - membership null`() = runTest {
        val client = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ ->
                Result.success(
                    aRoomPreview(
                        info = aRoomPreviewInfo(
                            roomId = A_ROOM_ID,
                            canonicalAlias = RoomAlias("#alias:matrix.org"),
                            name = "Room name",
                            topic = "Room topic",
                            avatarUrl = "avatarUrl",
                            numberOfJoinedMembers = 2,
                            isSpace = false,
                            isHistoryWorldReadable = false,
                            joinRule = JoinRule.Public,
                            currentUserMembership = null,
                        ),
                        roomMembershipDetails = {
                            Result.success(aRoomMembershipDetails())
                        },
                    )
                )
            },
            spaceService = FakeSpaceService(
                spaceRoomListResult = { FakeSpaceRoomList() },
            ),
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
                        roomAvatarUrl = "avatarUrl",
                        joinAuthorisationStatus = JoinAuthorisationStatus.CanJoin,
                        joinRule = JoinRule.Public,
                        details = aLoadedDetailsRoom(isDm = false),
                    )
                )
            }
        }
    }

    @Test
    fun `present - when room is not known RoomPreview is loaded - membership INVITED`() = runTest {
        val client = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ ->
                Result.success(
                    aRoomPreview(
                        info = aRoomPreviewInfo(
                            roomId = A_ROOM_ID,
                            canonicalAlias = RoomAlias("#alias:matrix.org"),
                            name = "Room name",
                            topic = "Room topic",
                            avatarUrl = "avatarUrl",
                            numberOfJoinedMembers = 2,
                            isSpace = false,
                            isHistoryWorldReadable = false,
                            joinRule = JoinRule.Public,
                            currentUserMembership = CurrentUserMembership.INVITED,
                        ),
                        roomMembershipDetails = {
                            Result.success(aRoomMembershipDetails())
                        }
                    )
                )
            },
            spaceService = FakeSpaceService(
                spaceRoomListResult = { FakeSpaceRoomList() },
            ),
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
                        roomAvatarUrl = "avatarUrl",
                        joinAuthorisationStatus = JoinAuthorisationStatus.IsInvited(
                            inviteData = InviteData(
                                roomId = A_ROOM_ID,
                                roomName = "Room name",
                                isDm = false,
                            ),
                            inviteSender = InviteSender(
                                userId = A_USER_ID_2,
                                displayName = "Bob",
                                avatarData = AvatarData(
                                    id = A_USER_ID_2.value,
                                    name = "Bob",
                                    size = AvatarSize.InviteSender,
                                ),
                                membershipChangeReason = null,
                            ),
                        ),
                        joinRule = JoinRule.Public,
                        details = aLoadedDetailsRoom(isDm = false),
                    )
                )
            }
        }
    }

    @Test
    fun `present - when room is not known RoomPreview is loaded - membership BANNED`() = runTest {
        val client = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ ->
                Result.success(
                    aRoomPreview(
                        info = aRoomPreviewInfo(
                            roomId = A_ROOM_ID,
                            canonicalAlias = RoomAlias("#alias:matrix.org"),
                            name = null,
                            topic = "Room topic",
                            avatarUrl = "avatarUrl",
                            numberOfJoinedMembers = 2,
                            isSpace = false,
                            isHistoryWorldReadable = false,
                            joinRule = JoinRule.Public,
                            currentUserMembership = CurrentUserMembership.BANNED,
                        ),
                        roomMembershipDetails = {
                            Result.success(
                                aRoomMembershipDetails(),
                            )
                        }
                    )
                )
            },
            spaceService = FakeSpaceService(
                spaceRoomListResult = { FakeSpaceRoomList() },
            ),
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
                        name = null,
                        topic = "Room topic",
                        alias = RoomAlias("#alias:matrix.org"),
                        numberOfMembers = 2,
                        roomAvatarUrl = "avatarUrl",
                        joinAuthorisationStatus = JoinAuthorisationStatus.IsBanned(
                            banSender = InviteSender(
                                userId = A_USER_ID_2,
                                displayName = "Bob",
                                avatarData = AvatarData(
                                    id = A_USER_ID_2.value,
                                    name = "Bob",
                                    size = AvatarSize.InviteSender,
                                ),
                                membershipChangeReason = null,
                            ),
                            reason = null,
                        ),
                        joinRule = JoinRule.Public,
                        details = aLoadedDetailsRoom(isDm = false),
                    )
                )
            }
        }
    }

    @Test
    fun `present - when room is not known RoomPreview is loaded - membership KNOCKED`() = runTest {
        val client = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ ->
                Result.success(
                    aRoomPreview(
                        info = aRoomPreviewInfo(
                            roomId = A_ROOM_ID,
                            canonicalAlias = RoomAlias("#alias:matrix.org"),
                            name = "Room name",
                            topic = "Room topic",
                            avatarUrl = "avatarUrl",
                            numberOfJoinedMembers = 2,
                            isSpace = false,
                            isHistoryWorldReadable = false,
                            joinRule = JoinRule.Public,
                            currentUserMembership = CurrentUserMembership.KNOCKED,
                        ),
                        roomMembershipDetails = {
                            Result.success(aRoomMembershipDetails())
                        }
                    )
                )
            },
            spaceService = FakeSpaceService(
                spaceRoomListResult = { FakeSpaceRoomList() },
            ),
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
                        roomAvatarUrl = "avatarUrl",
                        joinAuthorisationStatus = JoinAuthorisationStatus.IsKnocked,
                        joinRule = JoinRule.Public,
                        details = aLoadedDetailsRoom(isDm = false),
                    )
                )
            }
        }
    }

    @Test
    fun `present - when room is not known RoomPreview is loaded as Private`() = runTest {
        val client = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ ->
                Result.success(
                    aRoomPreview(
                        info = aRoomPreviewInfo(joinRule = JoinRule.Private),
                        roomMembershipDetails = {
                            Result.success(aRoomMembershipDetails())
                        },
                    )
                )
            },
            spaceService = FakeSpaceService(
                spaceRoomListResult = { FakeSpaceRoomList() },
            ),
        )
        val presenter = createJoinRoomPresenter(
            matrixClient = client
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.joinAuthorisationStatus).isEqualTo(JoinAuthorisationStatus.NeedInvite)
            }
        }
    }

    @Test
    fun `present - when room is not known RoomPreview is loaded as Custom`() = runTest {
        val client = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ ->
                Result.success(
                    aRoomPreview(
                        info = aRoomPreviewInfo(joinRule = JoinRule.Custom("custom")),
                        roomMembershipDetails = {
                            Result.success(aRoomMembershipDetails())
                        },
                    )
                )
            },
            spaceService = FakeSpaceService(
                spaceRoomListResult = { FakeSpaceRoomList() },
            ),
        )
        val presenter = createJoinRoomPresenter(
            matrixClient = client
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.joinAuthorisationStatus).isEqualTo(JoinAuthorisationStatus.Unknown)
            }
        }
    }

    @Test
    fun `present - when room is not known RoomPreview is loaded as Invite`() = runTest {
        val client = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ ->
                Result.success(
                    aRoomPreview(
                        info = aRoomPreviewInfo(joinRule = JoinRule.Invite),
                        roomMembershipDetails = {
                            Result.success(aRoomMembershipDetails())
                        },
                    )
                )
            },
            spaceService = FakeSpaceService(
                spaceRoomListResult = { FakeSpaceRoomList() },
            ),
        )
        val presenter = createJoinRoomPresenter(
            matrixClient = client
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.joinAuthorisationStatus).isEqualTo(JoinAuthorisationStatus.NeedInvite)
            }
        }
    }

    @Test
    fun `present - when room is not known RoomPreview is loaded as KnockRestricted`() = runTest {
        val client = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ ->
                Result.success(
                    aRoomPreview(
                        info = aRoomPreviewInfo(
                            joinRule = JoinRule.KnockRestricted(persistentListOf())
                        ),
                        roomMembershipDetails = {
                            Result.success(aRoomMembershipDetails())
                        }
                    )
                )
            },
            spaceService = FakeSpaceService(
                spaceRoomListResult = { FakeSpaceRoomList() },
            ),
        )
        val presenter = createJoinRoomPresenter(
            matrixClient = client
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.joinAuthorisationStatus).isEqualTo(JoinAuthorisationStatus.CanKnock)
            }
        }
    }

    @Test
    fun `present - when room is not known RoomPreview is loaded as Restricted`() = runTest {
        val client = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ ->
                Result.success(
                    aRoomPreview(
                        info = aRoomPreviewInfo(joinRule = JoinRule.Restricted(persistentListOf())),
                        roomMembershipDetails = {
                            Result.success(aRoomMembershipDetails())
                        },
                    )
                )
            },
            spaceService = FakeSpaceService(
                spaceRoomListResult = { FakeSpaceRoomList() },
            ),
        )
        val presenter = createJoinRoomPresenter(
            matrixClient = client
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.joinAuthorisationStatus).isEqualTo(JoinAuthorisationStatus.Restricted)
            }
        }
    }

    @Test
    fun `present - when room is not known RoomPreview is loaded with error`() = runTest {
        val client = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ ->
                Result.failure(AN_EXCEPTION)
            },
            spaceService = FakeSpaceService(
                spaceRoomListResult = { FakeSpaceRoomList() },
            ),
        )
        val presenter = createJoinRoomPresenter(
            matrixClient = client
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.contentState).isEqualTo(
                    ContentState.UnknownRoom
                )
            }
        }
    }

    @Test
    fun `present - when room is not known RoomPreview is loaded with error Forbidden`() = runTest {
        val client = FakeMatrixClient(
            getNotJoinedRoomResult = { _, _ ->
                Result.failure(ClientException.MatrixApi(ErrorKind.Forbidden, "403", "Forbidden", null))
            },
            spaceService = FakeSpaceService(
                spaceRoomListResult = { FakeSpaceRoomList() },
            ),
        )
        val presenter = createJoinRoomPresenter(
            matrixClient = client
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.contentState).isEqualTo(ContentState.UnknownRoom)
            }
        }
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

internal fun createJoinRoomPresenter(
    roomId: RoomId = A_ROOM_ID,
    roomDescription: Optional<RoomDescription> = Optional.empty(),
    serverNames: List<String> = emptyList(),
    trigger: JoinedRoom.Trigger = JoinedRoom.Trigger.Invite,
    matrixClient: MatrixClient = FakeMatrixClient(
        spaceService = FakeSpaceService(
            spaceRoomListResult = { FakeSpaceRoomList() },
        ),
    ),
    joinRoomLambda: (RoomIdOrAlias, List<String>, JoinedRoom.Trigger) -> Result<Unit> = { _, _, _ ->
        Result.success(Unit)
    },
    knockRoom: KnockRoom = FakeKnockRoom(),
    cancelKnockRoom: CancelKnockRoom = FakeCancelKnockRoom(),
    forgetRoom: ForgetRoom = FakeForgetRoom(),
    buildMeta: BuildMeta = aBuildMeta(applicationName = "AppName"),
    acceptDeclineInvitePresenter: Presenter<AcceptDeclineInviteState> = Presenter { anAcceptDeclineInviteState() },
    seenInvitesStore: SeenInvitesStore = InMemorySeenInvitesStore(),
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
        forgetRoom = forgetRoom,
        buildMeta = buildMeta,
        acceptDeclineInvitePresenter = acceptDeclineInvitePresenter,
        seenInvitesStore = seenInvitesStore,
    )
}

private fun aRoomMembershipDetails() = RoomMembershipDetails(
    currentUserMember = aRoomMember(userId = A_USER_ID, displayName = "Alice"),
    senderMember = aRoomMember(userId = A_USER_ID_2, displayName = "Bob"),
)
