/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.messagecomposer

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_ROOM_ALIAS
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.room.aRoomSummary
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultRoomAliasSuggestionsDataSourceTest {
    @Test
    fun `DefaultRoomAliasSuggestionsDataSource must emit a list of room alias suggestions`() = runTest {
        val roomListService = FakeRoomListService()
        val sut = DefaultRoomAliasSuggestionsDataSource(
            roomListService
        )
        val aRoomSummaryWithAnAlias = aRoomSummary(
            canonicalAlias = A_ROOM_ALIAS
        )
        sut.getAllRoomAliasSuggestions().test {
            assertThat(awaitItem()).isEmpty()
            roomListService.postAllRooms(
                listOf(
                    aRoomSummary(roomId = A_ROOM_ID_2, canonicalAlias = null),
                    aRoomSummaryWithAnAlias,
                )
            )
            assertThat(awaitItem()).isEqualTo(
                listOf(
                    RoomAliasSuggestion(
                        roomAlias = A_ROOM_ALIAS,
                        roomSummary = aRoomSummaryWithAnAlias
                    )
                )
            )
        }
    }
}
