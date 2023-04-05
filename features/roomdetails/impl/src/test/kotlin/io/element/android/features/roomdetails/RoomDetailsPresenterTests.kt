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

package io.element.android.features.roomdetails

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth
import io.element.android.features.roomdetails.impl.RoomDetailsPresenter
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoomDetailsPresenterTests {
    @Test
    fun `present - initial state is created from room info`() = runTest {
        val room = aMatrixRoom()
        val presenter = RoomDetailsPresenter(room)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.roomId).isEqualTo(room.roomId.value)
            Truth.assertThat(initialState.roomName).isEqualTo(room.name)
            Truth.assertThat(initialState.roomAvatarUrl).isEqualTo(room.avatarUrl)
            Truth.assertThat(initialState.roomTopic).isEqualTo(room.topic)
            Truth.assertThat(initialState.memberCount).isEqualTo(Async.Loading(null))
            Truth.assertThat(initialState.isEncrypted).isEqualTo(room.isEncrypted)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - room member count is calculated asynchronously`() = runTest {
        val room = aMatrixRoom()
        val presenter = RoomDetailsPresenter(room)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.memberCount).isEqualTo(Async.Loading(null))

            val finalState = awaitItem()
            Truth.assertThat(finalState.memberCount).isEqualTo(Async.Success(0))
        }
    }

    @Test
    fun `present - initial state with no room name`() = runTest {
        val room = aMatrixRoom(name = null)
        val presenter = RoomDetailsPresenter(room)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.roomName).isEqualTo(room.displayName)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - can handle error while fetching member count`() = runTest {
        val room = aMatrixRoom(name = null).apply {
            givenFetchMemberResult(Result.failure(Throwable()))
        }
        val presenter = RoomDetailsPresenter(room)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            Truth.assertThat(awaitItem().memberCount).isInstanceOf(Async.Failure::class.java)

            cancelAndIgnoreRemainingEvents()
        }
    }
}

fun aMatrixRoom(
    roomId: RoomId = A_ROOM_ID,
    name: String? = A_ROOM_NAME,
    displayName: String = "A fallback display name",
    topic: String? = "A topic",
    avatarUrl: String? = "https://matrix.org/avatar.jpg",
    members: List<RoomMember> = emptyList(),
    isEncrypted: Boolean = true,
) = FakeMatrixRoom(
    roomId = roomId,
    name = name,
    displayName = displayName,
    topic = topic,
    avatarUrl = avatarUrl,
    members = members,
    isEncrypted = isEncrypted,
)
