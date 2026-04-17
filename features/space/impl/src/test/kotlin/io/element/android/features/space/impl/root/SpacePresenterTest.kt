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
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteEvents
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteState
import io.element.android.features.invite.api.acceptdecline.anAcceptDeclineInviteState
import io.element.android.features.invite.api.toInviteData
import io.element.android.features.invite.test.InMemorySeenInvitesStore
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomType
import io.element.android.libraries.matrix.api.room.join.JoinRoom
import io.element.android.libraries.matrix.api.spaces.SpaceRoomList
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_ID_3
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.join.FakeJoinRoom
import io.element.android.libraries.matrix.test.room.powerlevels.FakeRoomPermissions
import io.element.android.libraries.matrix.test.spaces.FakeSpaceRoomList
import io.element.android.libraries.matrix.test.spaces.FakeSpaceService
import io.element.android.libraries.previewutils.room.aSpaceRoom
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import im.vector.app.features.analytics.plan.JoinedRoom as AnalyticsJoinedRoom

@RunWith(TestParameterInjector::class)
class SpacePresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val spaceRoomList = FakeSpaceRoomList(
            paginateResult = { Result.success(Unit) }
        )
        val presenter = createSpacePresenter(spaceRoomList = spaceRoomList)
        presenter.test {
            val state = awaitItem()
            assertThat(state.spaceInfo).isNotNull()
            assertThat(state.children).isEmpty()
            assertThat(state.seenSpaceInvites).isEmpty()
            assertThat(state.hideInvitesAvatar).isFalse()
            assertThat(state.hasMoreToLoad).isTrue()
            assertThat(state.joinActions).isEmpty()
            assertThat(state.acceptDeclineInviteState).isEqualTo(anAcceptDeclineInviteState())
            assertThat(state.topicViewerState).isEqualTo(TopicViewerState.Hidden)
            assertThat(state.canAccessSpaceSettings).isFalse()
        }
    }

    @Test
    fun `present - canAccessSpaceSettings true when has permissions`() = runTest {
        val room = FakeBaseRoom(
            roomPermissions = FakeRoomPermissions(
                canSendState = { true }
            )
        )
        val presenter = createSpacePresenter(
            room = room,
        )
        presenter.test {
            skipItems(1)
            val state = awaitItem()
            assertThat(state.canAccessSpaceSettings).isTrue()
        }
    }

    @Test
    fun `present - load more does nothing`() = runTest {
        // LoadMore event is a no-op as pagination is handled automatically for now as backend is slow.
        val spaceRoomList = FakeSpaceRoomList(
            paginateResult = { Result.success(Unit) }
        )
        val presenter = createSpacePresenter(spaceRoomList = spaceRoomList)
        presenter.test {
            val state = awaitItem()
            // LoadMore event should not cause any state change
            state.eventSink(SpaceEvents.LoadMore)
            expectNoEvents()
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
            assert(joinRoom).isCalledOnce().with(
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
    fun `present - invite action is transmitted to acceptDeclineInviteState`(
        @TestParameter acceptInvite: Boolean = namedTestValues(
            "accept" to true,
            "decline" to false,
        ),
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

    @Test
    fun `present - enter manage mode`() = runTest {
        val presenter = createSpacePresenter()
        presenter.test {
            val state = awaitItem()
            assertThat(state.isManageMode).isFalse()
            state.eventSink(SpaceEvents.EnterManageMode)
            val manageModeState = awaitItem()
            assertThat(manageModeState.isManageMode).isTrue()
            assertThat(manageModeState.selectedRoomIds).isEmpty()
        }
    }

    @Test
    fun `present - exit manage mode without removals does not call reset`() = runTest {
        val resetResult = lambdaRecorder<Result<Unit>>(ensureNeverCalled = true) { Result.success(Unit) }
        val fakeSpaceRoomList = FakeSpaceRoomList(
            paginateResult = { Result.success(Unit) },
            resetResult = resetResult,
        )
        val presenter = createSpacePresenter(spaceRoomList = fakeSpaceRoomList)
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(SpaceEvents.EnterManageMode)
            initialState.eventSink(SpaceEvents.ToggleRoomSelection(A_ROOM_ID))
            initialState.eventSink(SpaceEvents.ExitManageMode)
            advanceUntilIdle()
            val finalState = expectMostRecentItem()
            assertThat(finalState.isManageMode).isFalse()
            assertThat(finalState.selectedRoomIds).isEmpty()
            // reset should NOT be called since no rooms were actually removed
            assert(resetResult).isNeverCalled()
        }
    }

    @Test
    fun `present - toggle room selection`() = runTest {
        val presenter = createSpacePresenter()
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(SpaceEvents.EnterManageMode)
            // Select a room
            initialState.eventSink(SpaceEvents.ToggleRoomSelection(A_ROOM_ID))
            var latestState = expectMostRecentItem()
            assertThat(latestState.selectedRoomIds).containsExactly(A_ROOM_ID)
            // Deselect the room
            latestState.eventSink(SpaceEvents.ToggleRoomSelection(A_ROOM_ID))
            latestState = expectMostRecentItem()
            assertThat(latestState.selectedRoomIds).isEmpty()
        }
    }

    @Test
    fun `present - remove rooms success`() = runTest {
        val removeChildFromSpaceResult = lambdaRecorder<RoomId, RoomId, Result<Unit>> { _, _ ->
            Result.success(Unit)
        }
        val resetResult = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val aRoom = aSpaceRoom(
            roomId = A_ROOM_ID,
            roomType = RoomType.Room,
        )
        val fakeSpaceRoomList = FakeSpaceRoomList(
            initialSpaceRoomsValue = listOf(aRoom),
            paginateResult = { Result.success(Unit) },
            resetResult = resetResult,
        )
        val presenter = createSpacePresenter(
            spaceRoomList = fakeSpaceRoomList,
            spaceService = FakeSpaceService(
                removeChildFromSpaceResult = removeChildFromSpaceResult,
            ),
        )
        presenter.test {
            awaitItem() // Initial empty state
            advanceUntilIdle()
            val stateWithChildren = awaitItem()
            assertThat(stateWithChildren.children).hasSize(1)
            stateWithChildren.eventSink(SpaceEvents.EnterManageMode)
            stateWithChildren.eventSink(SpaceEvents.ToggleRoomSelection(A_ROOM_ID))
            stateWithChildren.eventSink(SpaceEvents.RemoveSelectedRooms)
            stateWithChildren.eventSink(SpaceEvents.ConfirmRoomRemoval)
            advanceUntilIdle()
            val successState = expectMostRecentItem()
            assertThat(successState.removeRoomsAction).isEqualTo(AsyncAction.Success(Unit))
            assertThat(successState.isManageMode).isFalse()
            assert(removeChildFromSpaceResult).isCalledOnce()
            assert(resetResult).isCalledOnce()
        }
    }

    @Test
    fun `present - remove rooms partial failure`() = runTest {
        val aRoom1 = aSpaceRoom(
            roomId = A_ROOM_ID,
            roomType = RoomType.Room,
        )
        val aRoom2 = aSpaceRoom(
            roomId = A_ROOM_ID_2,
            roomType = RoomType.Room,
        )
        val removeChildFromSpaceResult = lambdaRecorder<RoomId, RoomId, Result<Unit>> { _, childId ->
            if (childId == A_ROOM_ID_2) {
                Result.failure(AN_EXCEPTION)
            } else {
                Result.success(Unit)
            }
        }
        val fakeSpaceRoomList = FakeSpaceRoomList(
            initialSpaceRoomsValue = listOf(aRoom1, aRoom2),
            paginateResult = { Result.success(Unit) },
        )
        val presenter = createSpacePresenter(
            spaceRoomList = fakeSpaceRoomList,
            spaceService = FakeSpaceService(
                removeChildFromSpaceResult = removeChildFromSpaceResult,
            ),
        )
        presenter.test {
            awaitItem() // Initial empty state
            advanceUntilIdle()
            val stateWithChildren = awaitItem()
            assertThat(stateWithChildren.children).hasSize(2)
            stateWithChildren.eventSink(SpaceEvents.EnterManageMode)
            stateWithChildren.eventSink(SpaceEvents.ToggleRoomSelection(A_ROOM_ID))
            stateWithChildren.eventSink(SpaceEvents.ToggleRoomSelection(A_ROOM_ID_2))
            stateWithChildren.eventSink(SpaceEvents.RemoveSelectedRooms)
            stateWithChildren.eventSink(SpaceEvents.ConfirmRoomRemoval)
            advanceUntilIdle()
            val failureState = expectMostRecentItem()
            assertThat(failureState.removeRoomsAction.isFailure()).isTrue()
            // Successfully removed room should be filtered out
            assertThat(failureState.children.map { it.roomId }).doesNotContain(A_ROOM_ID)
            // Failed room should still be present
            assertThat(failureState.children.map { it.roomId }).contains(A_ROOM_ID_2)
            assert(removeChildFromSpaceResult).isCalledExactly(2)
        }
    }

    @Test
    fun `present - exit manage mode after partial failure calls reset`() = runTest {
        val aRoom1 = aSpaceRoom(
            roomId = A_ROOM_ID,
            roomType = RoomType.Room,
        )
        val aRoom2 = aSpaceRoom(
            roomId = A_ROOM_ID_2,
            roomType = RoomType.Room,
        )
        // Room 1 succeeds, Room 2 fails
        val removeChildFromSpaceResult = lambdaRecorder<RoomId, RoomId, Result<Unit>> { _, childId ->
            if (childId == A_ROOM_ID_2) {
                Result.failure(AN_EXCEPTION)
            } else {
                Result.success(Unit)
            }
        }
        val resetResult = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val fakeSpaceRoomList = FakeSpaceRoomList(
            initialSpaceRoomsValue = listOf(aRoom1, aRoom2),
            paginateResult = { Result.success(Unit) },
            resetResult = resetResult,
        )
        val presenter = createSpacePresenter(
            spaceRoomList = fakeSpaceRoomList,
            spaceService = FakeSpaceService(
                removeChildFromSpaceResult = removeChildFromSpaceResult,
            ),
        )
        presenter.test {
            awaitItem() // Initial empty state
            advanceUntilIdle()
            val stateWithChildren = awaitItem()
            stateWithChildren.eventSink(SpaceEvents.EnterManageMode)
            stateWithChildren.eventSink(SpaceEvents.ToggleRoomSelection(A_ROOM_ID))
            stateWithChildren.eventSink(SpaceEvents.ToggleRoomSelection(A_ROOM_ID_2))
            stateWithChildren.eventSink(SpaceEvents.RemoveSelectedRooms)
            stateWithChildren.eventSink(SpaceEvents.ConfirmRoomRemoval)
            advanceUntilIdle()
            val failureState = expectMostRecentItem()
            assertThat(failureState.removeRoomsAction.isFailure()).isTrue()
            // Exit manage mode after partial failure - reset should be called
            failureState.eventSink(SpaceEvents.ExitManageMode)
            advanceUntilIdle()
            expectMostRecentItem()
            assert(resetResult).isCalledOnce()
        }
    }

    @Test
    fun `present - children filtered in manage mode shows only rooms`() = runTest {
        val aRoom = aSpaceRoom(
            roomId = A_ROOM_ID,
            roomType = RoomType.Room,
        )
        val aSubSpace = aSpaceRoom(
            roomId = A_ROOM_ID_2,
            roomType = RoomType.Space,
        )
        val fakeSpaceRoomList = FakeSpaceRoomList(
            initialSpaceRoomsValue = listOf(aRoom, aSubSpace),
            paginateResult = { Result.success(Unit) },
        )
        val presenter = createSpacePresenter(spaceRoomList = fakeSpaceRoomList)
        presenter.test {
            awaitItem() // Initial empty state
            advanceUntilIdle()
            val stateWithChildren = awaitItem()
            // Both room and space visible initially
            assertThat(stateWithChildren.children).hasSize(2)
            assertThat(stateWithChildren.isManageMode).isFalse()
            stateWithChildren.eventSink(SpaceEvents.EnterManageMode)
            val manageModeState = expectMostRecentItem()
            // Only rooms visible in manage mode
            assertThat(manageModeState.children).hasSize(1)
            assertThat(manageModeState.children.first().roomId).isEqualTo(A_ROOM_ID)
            assertThat(manageModeState.children.first().isSpace).isFalse()
        }
    }

    @Test
    fun `present - removed rooms persist after flow update on partial failure`() = runTest {
        // On partial failure, successfully removed rooms should stay filtered even after flow updates
        val aRoom1 = aSpaceRoom(
            roomId = A_ROOM_ID,
            roomType = RoomType.Room,
        )
        val aRoom2 = aSpaceRoom(
            roomId = A_ROOM_ID_2,
            roomType = RoomType.Room,
        )
        val aRoom3 = aSpaceRoom(
            roomId = A_ROOM_ID_3,
            roomType = RoomType.Room,
        )
        // Room 1 succeeds, Room 2 fails
        val removeChildFromSpaceResult = lambdaRecorder<RoomId, RoomId, Result<Unit>> { _, childId ->
            if (childId == A_ROOM_ID_2) {
                Result.failure(AN_EXCEPTION)
            } else {
                Result.success(Unit)
            }
        }
        val spaceRoomList = FakeSpaceRoomList(
            initialSpaceRoomsValue = listOf(aRoom1, aRoom2),
            paginateResult = { Result.success(Unit) },
        )
        val presenter = createSpacePresenter(
            spaceRoomList = spaceRoomList,
            spaceService = FakeSpaceService(
                removeChildFromSpaceResult = removeChildFromSpaceResult,
            ),
        )
        presenter.test {
            awaitItem() // Initial empty state
            advanceUntilIdle()
            val stateWithChildren = awaitItem()
            stateWithChildren.eventSink(SpaceEvents.EnterManageMode)
            // Select both rooms for removal
            stateWithChildren.eventSink(SpaceEvents.ToggleRoomSelection(A_ROOM_ID))
            stateWithChildren.eventSink(SpaceEvents.ToggleRoomSelection(A_ROOM_ID_2))
            stateWithChildren.eventSink(SpaceEvents.RemoveSelectedRooms)
            stateWithChildren.eventSink(SpaceEvents.ConfirmRoomRemoval)
            advanceUntilIdle()
            val failureState = expectMostRecentItem()
            assertThat(failureState.removeRoomsAction.isFailure()).isTrue()
            // Successfully removed room should be filtered out
            assertThat(failureState.children.map { it.roomId }).doesNotContain(A_ROOM_ID)
            // Failed room should still be present
            assertThat(failureState.children.map { it.roomId }).contains(A_ROOM_ID_2)
            // Emit new flow update with a new room added (simulating server refresh)
            spaceRoomList.emitSpaceRooms(listOf(aRoom1, aRoom2, aRoom3))
            advanceUntilIdle()
            val afterFlowUpdate = awaitItem()
            // A_ROOM_ID should still be filtered out even though it's in the new emission
            assertThat(afterFlowUpdate.children.map { it.roomId }).doesNotContain(A_ROOM_ID)
            // But the other rooms should be present
            assertThat(afterFlowUpdate.children.map { it.roomId }).contains(A_ROOM_ID_2)
            assertThat(afterFlowUpdate.children.map { it.roomId }).contains(A_ROOM_ID_3)
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
        spaceService: FakeSpaceService = FakeSpaceService(),
    ): SpacePresenter {
        return SpacePresenter(
            client = client,
            room = room,
            spaceRoomList = spaceRoomList,
            seenInvitesStore = seenInvitesStore,
            joinRoom = joinRoom,
            acceptDeclineInvitePresenter = acceptDeclineInvitePresenter,
            sessionCoroutineScope = this,
            spaceService = spaceService,
        )
    }
}
