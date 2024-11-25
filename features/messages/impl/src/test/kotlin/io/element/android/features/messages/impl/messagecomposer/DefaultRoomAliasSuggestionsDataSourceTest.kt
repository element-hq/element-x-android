/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.messagecomposer

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.messagecomposer.suggestions.DefaultRoomAliasSuggestionsDataSource
import io.element.android.features.messages.impl.messagecomposer.suggestions.RoomAliasSuggestion
import io.element.android.libraries.matrix.test.A_ROOM_ALIAS
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.room.aRoomSummary
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultRoomAliasSuggestionsDataSourceTest {
    @Test
    fun `getAllRoomAliasSuggestions must emit a list of room alias suggestions`() = runTest {
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
                        roomId = aRoomSummaryWithAnAlias.roomId,
                        roomName = aRoomSummaryWithAnAlias.info.name,
                        roomAvatarUrl = aRoomSummaryWithAnAlias.info.avatarUrl
                    )
                )
            )
        }
    }
}
