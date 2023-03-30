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

package io.element.android.features.roomdetails.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.Async

open class RoomDetailsStateProvider : PreviewParameterProvider<RoomDetailsState> {
    override val values: Sequence<RoomDetailsState>
        get() = sequenceOf(
            aRoomDetailsState(),
            aRoomDetailsState().copy(roomTopic = null),
            aRoomDetailsState().copy(isEncrypted = false),
            aRoomDetailsState().copy(roomAlias = null),
            // Add other state here
        )
}

fun aRoomDetailsState() = RoomDetailsState(
    roomId = "a room id",
    roomName = "Marketing",
    roomAlias = "#marketing:domain.com",
    roomAvatarUrl = null,
    roomTopic = "Welcome to #marketing, home of the Marketing team " +
        "|| WIKI PAGE: https://domain.org/wiki/Marketing " +
        "|| MAIL iki/Marketing " +
        "|| MAI iki/Marketing " +
        "|| MAI iki/Marketing...",
    memberCount = Async.Success(32),
    isEncrypted = true,
//    eventSink = {}
)
