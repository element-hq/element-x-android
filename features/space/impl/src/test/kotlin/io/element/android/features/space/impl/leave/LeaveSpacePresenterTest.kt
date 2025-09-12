/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.space.impl.leave

import com.google.common.truth.Truth.assertThat
import io.element.android.features.space.api.SpaceEntryPoint
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SPACE_NAME
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.spaces.FakeSpaceRoomList
import io.element.android.libraries.matrix.test.spaces.FakeSpaceService
import io.element.android.libraries.previewutils.room.aSpaceRoom
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LeaveSpacePresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val paginateResult = lambdaRecorder<Result<Unit>> {
            Result.success(Unit)
        }
        val presenter = createLeaveSpacePresenter(
            matrixClient = FakeMatrixClient(
                spaceService = FakeSpaceService(
                    spaceRoomListResult = {
                        FakeSpaceRoomList(
                            paginateResult = paginateResult,
                        )
                    },
                ),
            ),
        )
        presenter.test {
            val state = awaitItem()
            assertThat(state.spaceName).isNull()
            assertThat(state.selectableSpaceRooms).isEqualTo(AsyncAction.Uninitialized)
            assertThat(state.leaveSpaceAction).isEqualTo(AsyncAction.Uninitialized)
            advanceUntilIdle()
            paginateResult.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - current space name`() = runTest {
        val fakeSpaceRoomList = FakeSpaceRoomList()
        val presenter = createLeaveSpacePresenter(
            matrixClient = FakeMatrixClient(
                spaceService = FakeSpaceService(
                    spaceRoomListResult = { fakeSpaceRoomList },
                ),
            ),
        )
        presenter.test {
            val state = awaitItem()
            advanceUntilIdle()
            assertThat(state.spaceName).isNull()
            val aSpace = aSpaceRoom(
                name = A_SPACE_NAME
            )
            fakeSpaceRoomList.emitCurrentSpace(aSpace)
            assertThat(awaitItem().spaceName).isEqualTo(A_SPACE_NAME)
        }
    }

    @Test
    fun `present - leave space and cancel`() = runTest {
        val fakeSpaceRoomList = FakeSpaceRoomList(
            paginateResult = { Result.success(Unit) },
        )
        val presenter = createLeaveSpacePresenter(
            matrixClient = FakeMatrixClient(
                spaceService = FakeSpaceService(
                    spaceRoomListResult = { fakeSpaceRoomList },
                ),
            ),
        )
        presenter.test {
            val state = awaitItem()
            advanceUntilIdle()
            assertThat(state.leaveSpaceAction).isEqualTo(AsyncAction.Uninitialized)
            state.eventSink(LeaveSpaceEvents.LeaveSpace)
            val stateAfterStarting = awaitItem()
            assertThat(stateAfterStarting.leaveSpaceAction).isInstanceOf(LeaveSpaceState::class.java)
            val shown = stateAfterStarting.leaveSpaceAction as LeaveSpaceState
            assertThat(shown.spaceName).isNull()
            assertThat(shown.selectableSpaceRooms).isInstanceOf(AsyncData.Loading::class.java)
            val stateAfterLoading = awaitItem()
            val shownLoaded = stateAfterLoading.leaveSpaceAction as LeaveSpaceState
            assertThat(shownLoaded.selectableSpaceRooms.dataOrNull()!!).isEmpty()
            stateAfterLoading.eventSink(LeaveSpaceEvents.CloseError)
            val stateAfterCancel = awaitItem()
            assertThat(stateAfterCancel.leaveSpaceAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `present - leave space and confirm`() = runTest {
        val fakeSpaceRoomList = FakeSpaceRoomList(
            paginateResult = { Result.success(Unit) },
        )
        val leaveRoomLambda = lambdaRecorder<Result<Unit>> {
            Result.success(Unit)
        }
        val presenter = createLeaveSpacePresenter(
            matrixClient = FakeMatrixClient(
                spaceService = FakeSpaceService(
                    spaceRoomListResult = { fakeSpaceRoomList },
                ),
            ).apply {
                givenGetRoomResult(
                    roomId = A_ROOM_ID,
                    result = FakeBaseRoom(
                        leaveRoomLambda = leaveRoomLambda,
                    )
                )
            },
        )
        presenter.test {
            val state = awaitItem()
            advanceUntilIdle()
            assertThat(state.leaveSpaceAction).isEqualTo(AsyncAction.Uninitialized)
            state.eventSink(LeaveSpaceEvents.LeaveSpace)
            val stateAfterStarting = awaitItem()
            assertThat(stateAfterStarting.leaveSpaceAction).isInstanceOf(LeaveSpaceState::class.java)
            val shown = stateAfterStarting.leaveSpaceAction as LeaveSpaceState
            assertThat(shown.spaceName).isNull()
            assertThat(shown.selectableSpaceRooms).isInstanceOf(AsyncData.Loading::class.java)
            val stateAfterLoading = awaitItem()
            val shownLoaded = stateAfterLoading.leaveSpaceAction as LeaveSpaceState
            assertThat(shownLoaded.selectableSpaceRooms.dataOrNull()!!).isEmpty()
            stateAfterLoading.eventSink(LeaveSpaceEvents.LeaveSpace)
            val stateLoading = awaitItem()
            assertThat(stateLoading.leaveSpaceAction).isEqualTo(AsyncAction.Loading)
            val stateFinal = awaitItem()
            assertThat(stateFinal.leaveSpaceAction).isEqualTo(AsyncAction.Success(Unit))
            leaveRoomLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - leave space, confirm then failure`() = runTest {
        val fakeSpaceRoomList = FakeSpaceRoomList(
            paginateResult = { Result.success(Unit) },
        )
        val leaveRoomLambda = lambdaRecorder<Result<Unit>> {
            Result.failure(AN_EXCEPTION)
        }
        val presenter = createLeaveSpacePresenter(
            matrixClient = FakeMatrixClient(
                spaceService = FakeSpaceService(
                    spaceRoomListResult = { fakeSpaceRoomList },
                ),
            ).apply {
                givenGetRoomResult(
                    roomId = A_ROOM_ID,
                    result = FakeBaseRoom(
                        leaveRoomLambda = leaveRoomLambda,
                    )
                )
            },
        )
        presenter.test {
            val state = awaitItem()
            advanceUntilIdle()
            assertThat(state.leaveSpaceAction).isEqualTo(AsyncAction.Uninitialized)
            state.eventSink(LeaveSpaceEvents.LeaveSpace)
            val stateAfterStarting = awaitItem()
            assertThat(stateAfterStarting.leaveSpaceAction).isInstanceOf(LeaveSpaceState::class.java)
            val shown = stateAfterStarting.leaveSpaceAction as LeaveSpaceState
            assertThat(shown.spaceName).isNull()
            assertThat(shown.selectableSpaceRooms).isInstanceOf(AsyncData.Loading::class.java)
            val stateAfterLoading = awaitItem()
            val shownLoaded = stateAfterLoading.leaveSpaceAction as LeaveSpaceState
            assertThat(shownLoaded.selectableSpaceRooms.dataOrNull()!!).isEmpty()
            stateAfterLoading.eventSink(LeaveSpaceEvents.LeaveSpace)
            val stateLoading = awaitItem()
            assertThat(stateLoading.leaveSpaceAction).isEqualTo(AsyncAction.Loading)
            val stateError = awaitItem()
            assertThat(stateError.leaveSpaceAction).isEqualTo(AsyncAction.Failure(AN_EXCEPTION))
            leaveRoomLambda.assertions().isCalledOnce()
            // Close error
            stateError.eventSink(LeaveSpaceEvents.CloseError)
            val stateFinal = awaitItem()
            assertThat(stateFinal.leaveSpaceAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    private fun createLeaveSpacePresenter(
        inputs: SpaceEntryPoint.Inputs = SpaceEntryPoint.Inputs(A_ROOM_ID),
        matrixClient: MatrixClient = FakeMatrixClient(),
    ): LeaveSpacePresenter {
        return LeaveSpacePresenter(
            inputs = inputs,
            matrixClient = matrixClient,
        )
    }
}
