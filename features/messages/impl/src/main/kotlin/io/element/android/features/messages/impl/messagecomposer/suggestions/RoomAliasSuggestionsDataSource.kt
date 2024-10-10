/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.messagecomposer.suggestions

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class RoomAliasSuggestion(
    val roomAlias: RoomAlias,
    val roomId: RoomId,
    val roomName: String?,
    val roomAvatarUrl: String?,
)

interface RoomAliasSuggestionsDataSource {
    fun getAllRoomAliasSuggestions(): Flow<List<RoomAliasSuggestion>>
}

@ContributesBinding(SessionScope::class)
class DefaultRoomAliasSuggestionsDataSource @Inject constructor(
    private val roomListService: RoomListService,
) : RoomAliasSuggestionsDataSource {
    override fun getAllRoomAliasSuggestions(): Flow<List<RoomAliasSuggestion>> {
        return roomListService
            .allRooms
            .summaries
            .map { roomSummaries ->
                roomSummaries
                    .mapNotNull { roomSummary ->
                        roomSummary.info.canonicalAlias?.let { roomAlias ->
                            RoomAliasSuggestion(
                                roomAlias = roomAlias,
                                roomId = roomSummary.roomId,
                                roomName = roomSummary.info.name,
                                roomAvatarUrl = roomSummary.info.avatarUrl,
                            )
                        }
                    }
            }
    }
}
