/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.space.impl.root

import com.google.common.truth.Truth.assertThat
import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteEvents
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteState
import io.element.android.features.invite.api.acceptdecline.anAcceptDeclineInviteState
import io.element.android.features.invite.api.toInviteData
import io.element.android.features.invite.test.InMemorySeenInvitesStore
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.join.JoinRoom
import io.element.android.libraries.matrix.api.spaces.SpaceRoomList
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.join.FakeJoinRoom
import io.element.android.libraries.matrix.test.room.powerlevels.FakeRoomPermissions
import io.element.android.libraries.matrix.test.spaces.FakeSpaceRoomList
import io.element.android.libraries.previewutils.room.aSpaceRoom
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import im.vector.app.features.analytics.plan.JoinedRoom as AnalyticsJoinedRoom

class SpacePresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val paginateResult = lambdaRecorder<Result<Unit>> {
            Result.success(Unit)
        }
        val spaceRoomList = FakeSpaceRoomList(paginateResult = paginateResult)
        val presenter = createSpacePresenter(spaceRoomList = spaceRoomList)
        presenter.test {
            val state = awaitItem()
            assertThat(state.currentSpace).isNull()
            assertThat(state.children).isEmpty()
            assertThat(state.seenSpaceInvites).isEmpty()
            assertThat(state.hideInvitesAvatar).isFalse()
            assertThat(state.hasMoreToLoad).isTrue()
            assertThat(state.joinActions).isEmpty()
            assertThat(state.acceptDeclineInviteState).isEqualTo(anAcceptDeclineInviteState())
            assertThat(state.topicViewerState).isEqualTo(TopicViewerState.Hidden)
            assertThat(state.canAccessSpaceSettings).isFalse()
            advanceUntilIdle()
            paginateResult.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - canAccessSpaceSettings false when space settings ff is enabled but no permissions`() = runTest {
        val presenter = createSpacePresenter(spaceSettingsEnabled = true)
        presenter.test {
            val state = awaitItem()
            assertThat(state.canAccessSpaceSettings).isFalse()
        }
    }

    @Test
    fun `present - canAccessSpaceSettings true when space settings ff is enabled and has permissions`() = runTest {
        val room = FakeBaseRoom(
            roomPermissions = FakeRoomPermissions(
                canSendState = { true }
            )
        )
        val presenter = createSpacePresenter(
            room = room,
            spaceSettingsEnabled = true,
        )
        presenter.test {
            skipItems(1)
            val state = awaitItem()
            assertThat(state.canAccessSpaceSettings).isTrue()
        }
    }

    @Test
    fun `present - load more`() = runTest {
        val paginateResult = lambdaRecorder<Result<Unit>> {
            Result.success(Unit)
        }
        val spaceRoomList = FakeSpaceRoomList(paginateResult = paginateResult)
        val presenter = createSpacePresenter(spaceRoomList = spaceRoomList)
        presenter.test {
            val state = awaitItem()
            advanceUntilIdle()
            paginateResult.assertions().isCalledOnce()
            state.eventSink(SpaceEvents.LoadMore)
            advanceUntilIdle()
            paginateResult.assertions().isCalledExactly(2)
        }
    }

    @Test
    fun `present - has more to load value`() = runTest {
        val paginateResult = lambdaRecorder<Result<Unit>> {
            Result.success(Unit)
        }
        val spaceRoomList = FakeSpaceRoomList(paginateResult = paginateResult)
        val presenter = createSpacePresenter(spaceRoomList = spaceRoomList)
        presenter.test {
            val state = awaitItem()
            advanceUntilIdle()
            assertThat(state.hasMoreToLoad).isTrue()
            spaceRoomList.emitPaginationStatus(
                SpaceRoomList.PaginationStatus.Idle(hasMoreToLoad = false)
            )
            assertThat(awaitItem().hasMoreToLoad).isFalse()
            spaceRoomList.emitPaginationStatus(
                SpaceRoomList.PaginationStatus.Idle(hasMoreToLoad = true)
            )
            assertThat(awaitItem().hasMoreToLoad).isTrue()
        }
    }

    @Test
    fun `present - current space value`() = runTest {
        val paginateResult = lambdaRecorder<Result<Unit>> {
            Result.success(Unit)
        }
        val spaceRoomList = FakeSpaceRoomList(paginateResult = paginateResult)
        val presenter = createSpacePresenter(spaceRoomList = spaceRoomList)
        presenter.test {
            val state = awaitItem()
            advanceUntilIdle()
            assertThat(state.currentSpace).isNull()
            val aSpace = aSpaceRoom()
            spaceRoomList.emitCurrentSpace(aSpace)
            assertThat(awaitItem().currentSpace).isEqualTo(aSpace)
        }
    }

    @Test
    fun `present - children value`() = runTest {
        val paginateResult = lambdaRecorder<Result<Unit>> {
            Result.success(Unit)
        }
        val spaceRoomList = FakeSpaceRoomList(paginateResult = paginateResult)
        val presenter = createSpacePresenter(spaceRoomList = spaceRoomList)
        presenter.test {
            val state = awaitItem()
            advanceUntilIdle()
            assertThat(state.children).isEmpty()
            val aSpace = aSpaceRoom()
            spaceRoomList.emitSpaceRooms(listOf(aSpace))
            assertThat(awaitItem().children).containsExactly(aSpace)
        }
    }

    @Test
    fun `present - join a room success`() = runTest {
        val joinRoom = lambdaRecorder<RoomIdOrAlias, List<String>, AnalyticsJoinedRoom.Trigger, Result<Unit>> { _, _, _ ->
            Result.success(Unit)
        }
        val serverNames = listOf("via1", "via2")
        val aNotJoinedRoom = aSpaceRoom(
            roomId = A_ROOM_ID_2,
            via = serverNames,
            state = null,
        )
        val fakeSpaceRoomList = FakeSpaceRoomList(
            initialSpaceRoomsValue = listOf(
                aSpaceRoom(
                    roomId = A_ROOM_ID,
                    state = CurrentUserMembership.JOINED,
                ),
                aNotJoinedRoom,
            ),
            paginateResult = { Result.success(Unit) },
        )
        val presenter = createSpacePresenter(
            spaceRoomList = fakeSpaceRoomList,
            joinRoom = FakeJoinRoom(
                lambda = joinRoom,
            ),
        )
        presenter.test {
            skipItems(1)
            val state = awaitItem()
            assertThat(state.joinActions[A_ROOM_ID_2]).isNull()
            state.eventSink(SpaceEvents.Join(aNotJoinedRoom))
            val joiningState = awaitItem()
            assertThat(joiningState.joinActions[A_ROOM_ID_2]).isEqualTo(AsyncAction.Loading)
            // Let the joinRoom call complete
            advanceUntilIdle()
            runCurrent()
            // The room is joined
            fakeSpaceRoomList.emitSpaceRooms(
                listOf(
                    aSpaceRoom(
                        roomId = A_ROOM_ID,
                        state = CurrentUserMembership.JOINED,
                    ),
                    aNotJoinedRoom.copy(state = CurrentUserMembership.JOINED),
                )
            )
            skipItems(1)
            val joinedState = awaitItem()
            // Joined room is removed from the join actions
            assertThat(joinedState.joinActions).doesNotContainKey(A_ROOM_ID_2)
            joinRoom.assertions().isCalledOnce().with(
                value(A_ROOM_ID_2.toRoomIdOrAlias()),
                value(serverNames),
                value(AnalyticsJoinedRoom.Trigger.SpaceHierarchy),
            )
        }
    }

    @Test
    fun `present - join a room failure`() = runTest {
        val aNotJoinedRoom = aSpaceRoom(
            roomId = A_ROOM_ID_2,
            state = null,
        )
        val fakeSpaceRoomList = FakeSpaceRoomList(
            initialSpaceRoomsValue = listOf(
                aSpaceRoom(
                    roomId = A_ROOM_ID,
                    state = CurrentUserMembership.JOINED,
                ),
                aNotJoinedRoom,
            ),
            paginateResult = { Result.success(Unit) },
        )
        val presenter = createSpacePresenter(
            spaceRoomList = fakeSpaceRoomList,
            joinRoom = FakeJoinRoom(
                lambda = { _, _, _ -> Result.failure(AN_EXCEPTION) },
            ),
        )
        presenter.test {
            skipItems(1)
            val state = awaitItem()
            assertThat(state.joinActions[A_ROOM_ID_2]).isNull()
            state.eventSink(SpaceEvents.Join(aNotJoinedRoom))
            val joiningState = awaitItem()
            assertThat(joiningState.joinActions[A_ROOM_ID_2]).isEqualTo(AsyncAction.Loading)
            val errorState = awaitItem()
            // Joined room is removed from the join actions
            assertThat(errorState.joinActions[A_ROOM_ID_2]!!.isFailure()).isTrue()
            // Clear error
            errorState.eventSink(SpaceEvents.ClearFailures)
            val clearedState = awaitItem()
            assertThat(clearedState.joinActions[A_ROOM_ID_2]).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `present - topic viewer state`() = runTest {
        val paginateResult = lambdaRecorder<Result<Unit>> {
            Result.success(Unit)
        }
        val spaceRoomList = FakeSpaceRoomList(paginateResult = paginateResult)
        val presenter = createSpacePresenter(spaceRoomList = spaceRoomList)
        presenter.test {
            val state = awaitItem()
            assertThat(state.topicViewerState).isEqualTo(TopicViewerState.Hidden)
            advanceUntilIdle()
            state.eventSink(SpaceEvents.ShowTopicViewer("topic"))
            assertThat(awaitItem().topicViewerState).isEqualTo(TopicViewerState.Shown("topic"))
            state.eventSink(SpaceEvents.HideTopicViewer)
            assertThat(awaitItem().topicViewerState).isEqualTo(TopicViewerState.Hidden)
        }
    }

    @Test
    fun `present - accept invite is transmitted to acceptDeclineInviteState`() {
        `invite action is transmitted to acceptDeclineInviteState`(
            acceptInvite = true,
        )
    }

    @Test
    fun `present - decline invite is transmitted to acceptDeclineInviteState`() {
        `invite action is transmitted to acceptDeclineInviteState`(
            acceptInvite = false,
        )
    }

    private fun `invite action is transmitted to acceptDeclineInviteState`(
        acceptInvite: Boolean,
    ) = runTest {
        val eventRecorder = EventsRecorder<AcceptDeclineInviteEvents>()
        val anInvitedRoom = aSpaceRoom(
            roomId = A_ROOM_ID_2,
            state = CurrentUserMembership.INVITED,
        )
        val fakeSpaceRoomList = FakeSpaceRoomList(
            initialSpaceRoomsValue = listOf(
                aSpaceRoom(
                    roomId = A_ROOM_ID,
                    state = CurrentUserMembership.JOINED,
                ),
                anInvitedRoom,
            ),
            paginateResult = { Result.success(Unit) },
        )
        val presenter = createSpacePresenter(
            spaceRoomList = fakeSpaceRoomList,
            acceptDeclineInvitePresenter = {
                anAcceptDeclineInviteState(
                    eventSink = eventRecorder,
                )
            },
        )
        presenter.test {
            skipItems(1)
            val state = awaitItem()
            assertThat(state.joinActions[A_ROOM_ID_2]).isNull()
            if (acceptInvite) {
                state.eventSink(SpaceEvents.AcceptInvite(anInvitedRoom))
                eventRecorder.assertSingle(
                    AcceptDeclineInviteEvents.AcceptInvite(
                        invite = anInvitedRoom.toInviteData(),
                    )
                )
            } else {
                state.eventSink(SpaceEvents.DeclineInvite(anInvitedRoom))
                eventRecorder.assertSingle(
                    AcceptDeclineInviteEvents.DeclineInvite(
                        invite = anInvitedRoom.toInviteData(),
                        shouldConfirm = true,
                        blockUser = false,
                    )
                )
            }
        }
    }

    private fun TestScope.createSpacePresenter(
        client: MatrixClient = FakeMatrixClient(),
        room: BaseRoom = FakeBaseRoom(),
        spaceRoomList: SpaceRoomList = FakeSpaceRoomList(
            paginateResult = { Result.success(Unit) }
        ),
        seenInvitesStore: SeenInvitesStore = InMemorySeenInvitesStore(),
        joinRoom: JoinRoom = FakeJoinRoom(
            lambda = { _, _, _ -> Result.success(Unit) },
        ),
        acceptDeclineInvitePresenter: Presenter<AcceptDeclineInviteState> = Presenter { anAcceptDeclineInviteState() },
        spaceSettingsEnabled: Boolean = false,
    ): SpacePresenter {
        return SpacePresenter(
            client = client,
            room = room,
            spaceRoomList = spaceRoomList,
            seenInvitesStore = seenInvitesStore,
            joinRoom = joinRoom,
            acceptDeclineInvitePresenter = acceptDeclineInvitePresenter,
            sessionCoroutineScope = backgroundScope,
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(
                    FeatureFlags.SpaceSettings.key to spaceSettingsEnabled,
                )
            ),
        )
    }
}
