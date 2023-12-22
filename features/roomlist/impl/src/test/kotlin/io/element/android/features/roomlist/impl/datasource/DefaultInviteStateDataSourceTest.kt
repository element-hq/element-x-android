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

package io.element.android.features.roomlist.impl.datasource

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.invitelist.test.FakeSeenInvitesStore
import io.element.android.features.roomlist.impl.InvitesState
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.aRoomSummaryFilled
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class DefaultInviteStateDataSourceTest {

    @Test
    fun `emits NoInvites state if invites list is empty`() = runTest {
        val roomListService = FakeRoomListService()
        val client = FakeMatrixClient(roomListService = roomListService)
        val seenStore = FakeSeenInvitesStore()
        val dataSource = DefaultInviteStateDataSource(client, seenStore, testCoroutineDispatchers())

        moleculeFlow(RecompositionMode.Immediate) {
            dataSource.inviteState()
        }.test {
            assertThat(awaitItem()).isEqualTo(InvitesState.NoInvites)
        }
    }

    @Test
    fun `emits NewInvites state if unseen invite exists`() = runTest {
        val roomListService = FakeRoomListService()
        roomListService.postInviteRooms(listOf(aRoomSummaryFilled(roomId = A_ROOM_ID)))
        val client = FakeMatrixClient(roomListService = roomListService)
        val seenStore = FakeSeenInvitesStore()
        val dataSource = DefaultInviteStateDataSource(client, seenStore, testCoroutineDispatchers())

        moleculeFlow(RecompositionMode.Immediate) {
            dataSource.inviteState()
        }.test {
            skipItems(1)
            assertThat(awaitItem()).isEqualTo(InvitesState.NewInvites)
        }
    }

    @Test
    fun `emits NewInvites state if multiple invites exist and at least one is unseen`() = runTest {
        val roomListService = FakeRoomListService()
        roomListService.postInviteRooms(listOf(aRoomSummaryFilled(roomId = A_ROOM_ID), aRoomSummaryFilled(roomId = A_ROOM_ID_2)))
        val client = FakeMatrixClient(roomListService = roomListService)
        val seenStore = FakeSeenInvitesStore()
        seenStore.publishRoomIds(setOf(A_ROOM_ID))
        val dataSource = DefaultInviteStateDataSource(client, seenStore, testCoroutineDispatchers(useUnconfinedTestDispatcher = true))

        moleculeFlow(RecompositionMode.Immediate) {
            dataSource.inviteState()
        }.test {
            skipItems(1)
            assertThat(awaitItem()).isEqualTo(InvitesState.NewInvites)
        }
    }

    @Test
    fun `emits SeenInvites state if invite exists in seen store`() = runTest {
        val roomListService = FakeRoomListService()
        roomListService.postInviteRooms(listOf(aRoomSummaryFilled(roomId = A_ROOM_ID)))
        val client = FakeMatrixClient(roomListService = roomListService)
        val seenStore = FakeSeenInvitesStore()
        seenStore.publishRoomIds(setOf(A_ROOM_ID))
        val dataSource = DefaultInviteStateDataSource(client, seenStore, testCoroutineDispatchers(useUnconfinedTestDispatcher = true))

        moleculeFlow(RecompositionMode.Immediate) {
            dataSource.inviteState()
        }.test {
            skipItems(1)

            assertThat(awaitItem()).isEqualTo(InvitesState.SeenInvites)
        }
    }

    @Test
    fun `emits new state in response to upstream events`() = runTest {
        val roomListService = FakeRoomListService()
        val client = FakeMatrixClient(roomListService = roomListService)
        val seenStore = FakeSeenInvitesStore()
        val dataSource = DefaultInviteStateDataSource(client, seenStore, testCoroutineDispatchers())

        moleculeFlow(RecompositionMode.Immediate) {
            dataSource.inviteState()
        }.test {
            // Initially there are no invites
            assertThat(awaitItem()).isEqualTo(InvitesState.NoInvites)

            // When a single invite is received, state should be NewInvites
            roomListService.postInviteRooms(listOf(aRoomSummaryFilled(roomId = A_ROOM_ID)))
            skipItems(1)
            assertThat(awaitItem()).isEqualTo(InvitesState.NewInvites)

            // If that invite is marked as seen, then the state becomes SeenInvites
            seenStore.publishRoomIds(setOf(A_ROOM_ID))
            skipItems(1)
            assertThat(awaitItem()).isEqualTo(InvitesState.SeenInvites)

            // Another new invite resets it to NewInvites
            roomListService.postInviteRooms(listOf(aRoomSummaryFilled(roomId = A_ROOM_ID), aRoomSummaryFilled(roomId = A_ROOM_ID_2)))
            skipItems(1)
            assertThat(awaitItem()).isEqualTo(InvitesState.NewInvites)

            // All of the invites going away reverts to NoInvites
            roomListService.postInviteRooms(emptyList())
            skipItems(1)
            assertThat(awaitItem()).isEqualTo(InvitesState.NoInvites)
        }
    }
}
