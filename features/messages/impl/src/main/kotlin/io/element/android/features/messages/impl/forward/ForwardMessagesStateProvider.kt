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

package io.element.android.features.messages.impl.forward

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomSummaryDetails
import io.element.android.libraries.matrix.api.room.message.RoomMessage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

open class ForwardMessagesStateProvider : PreviewParameterProvider<ForwardMessagesState> {
    override val values: Sequence<ForwardMessagesState>
        get() = sequenceOf(
            aForwardMessagesState(),
            aForwardMessagesState(
                resultState = SearchBarResultState.Results(
                    persistentListOf(
                        aRoomDetailsState(),
                        aRoomDetailsState(roomId = RoomId("!room2:domain"), canonicalAlias = "#element-x-room:matrix.org"),
                    )
                ),
            ),
            // Add other states here
        )
}

// TODO: Add more states here
fun aForwardMessagesState(
    resultState: SearchBarResultState<ImmutableList<RoomSummaryDetails>> = SearchBarResultState.NotSearching(),
) = ForwardMessagesState(
    resultState = resultState,
    query = "",
    isSearchActive = false,
    selectedRooms = persistentListOf(),
    isForwarding = false,
    error = null,
    forwardingSucceeded = null,
    eventSink = {}
)

fun aRoomDetailsState(
    roomId: RoomId = RoomId("!room:domain"),
    name: String = "roomName",
    canonicalAlias: String? = null,
    isDirect: Boolean = true,
    avatarURLString: String? = null,
    lastMessage: RoomMessage? = null,
    lastMessageTimestamp: Long? = null,
    unreadNotificationCount: Int = 0,
    inviter: RoomMember? = null,
) = RoomSummaryDetails(
        roomId = roomId,
        name = name,
        canonicalAlias = canonicalAlias,
        isDirect = isDirect,
        avatarURLString = avatarURLString,
        lastMessage = lastMessage,
        lastMessageTimestamp = lastMessageTimestamp,
        unreadNotificationCount = unreadNotificationCount,
        inviter = inviter,
    )
