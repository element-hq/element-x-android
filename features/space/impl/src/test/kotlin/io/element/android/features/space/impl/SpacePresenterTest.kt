/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.space.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.features.invite.test.InMemorySeenInvitesStore
import io.element.android.features.space.api.SpaceEntryPoint
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.spaces.SpaceRoomList
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.spaces.FakeSpaceRoomList
import io.element.android.libraries.matrix.test.spaces.FakeSpaceService
import io.element.android.libraries.previewutils.room.aSpaceRoom
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SpacePresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val paginateResult = lambdaRecorder<Result<Unit>> {
            Result.success(Unit)
        }
        val presenter = createSpacePresenter(
            client = FakeMatrixClient(
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
            assertThat(state.currentSpace).isNull()
            assertThat(state.children).isEmpty()
            assertThat(state.seenSpaceInvites).isEmpty()
            assertThat(state.hideInvitesAvatar).isFalse()
            assertThat(state.hasMoreToLoad).isTrue()
            advanceUntilIdle()
            paginateResult.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - load more`() = runTest {
        val paginateResult = lambdaRecorder<Result<Unit>> {
            Result.success(Unit)
        }
        val presenter = createSpacePresenter(
            client = FakeMatrixClient(
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
            advanceUntilIdle()
            paginateResult.assertions().isCalledOnce()
            state.eventSink(SpaceEvents.LoadMore)
            advanceUntilIdle()
            paginateResult.assertions().isCalledExactly(2)
        }
    }

    @Test
    fun `present - has more to load value`() = runTest {
        val fakeSpaceRoomList = FakeSpaceRoomList(
            paginateResult = { Result.success(Unit) },
        )
        val presenter = createSpacePresenter(
            client = FakeMatrixClient(
                spaceService = FakeSpaceService(
                    spaceRoomListResult = { fakeSpaceRoomList },
                ),
            ),
        )
        presenter.test {
            val state = awaitItem()
            advanceUntilIdle()
            assertThat(state.hasMoreToLoad).isTrue()
            fakeSpaceRoomList.emitPaginationStatus(
                SpaceRoomList.PaginationStatus.Idle(hasMoreToLoad = false)
            )
            assertThat(awaitItem().hasMoreToLoad).isFalse()
            fakeSpaceRoomList.emitPaginationStatus(
                SpaceRoomList.PaginationStatus.Idle(hasMoreToLoad = true)
            )
            assertThat(awaitItem().hasMoreToLoad).isTrue()
        }
    }

    @Test
    fun `present - current space value`() = runTest {
        val fakeSpaceRoomList = FakeSpaceRoomList(
            paginateResult = { Result.success(Unit) },
        )
        val presenter = createSpacePresenter(
            client = FakeMatrixClient(
                spaceService = FakeSpaceService(
                    spaceRoomListResult = { fakeSpaceRoomList },
                ),
            ),
        )
        presenter.test {
            val state = awaitItem()
            advanceUntilIdle()
            assertThat(state.currentSpace).isNull()
            val aSpace = aSpaceRoom()
            fakeSpaceRoomList.emitCurrentSpace(aSpace)
            assertThat(awaitItem().currentSpace).isEqualTo(aSpace)
        }
    }

    @Test
    fun `present - children value`() = runTest {
        val fakeSpaceRoomList = FakeSpaceRoomList(
            paginateResult = { Result.success(Unit) },
        )
        val presenter = createSpacePresenter(
            client = FakeMatrixClient(
                spaceService = FakeSpaceService(
                    spaceRoomListResult = { fakeSpaceRoomList },
                ),
            ),
        )
        presenter.test {
            val state = awaitItem()
            advanceUntilIdle()
            assertThat(state.children).isEmpty()
            val aSpace = aSpaceRoom()
            fakeSpaceRoomList.emitSpaceRooms(listOf(aSpace))
            assertThat(awaitItem().children).containsExactly(aSpace)
        }
    }

    private fun createSpacePresenter(
        inputs: SpaceEntryPoint.Inputs = SpaceEntryPoint.Inputs(A_ROOM_ID),
        client: MatrixClient = FakeMatrixClient(),
        seenInvitesStore: SeenInvitesStore = InMemorySeenInvitesStore(),
    ): SpacePresenter {
        return SpacePresenter(
            inputs = inputs,
            client = client,
            seenInvitesStore = seenInvitesStore,
        )
    }
}
