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

open class RoomDetailsStateProvider : PreviewParameterProvider<RoomDetailsState> {
    override val values: Sequence<RoomDetailsState>
        get() = sequenceOf(
            aTemplateState(),
            aTemplateState().copy(roomTopic = null),
            aTemplateState().copy(isEncrypted = false),
            // Add other state here
        )
}

fun aTemplateState() = RoomDetailsState(
    roomId = "#marketing:domain.com",
    roomName = "Marketing",
    roomAvatarUrl = null,
    roomTopic = "Welcome to #marketing, home of the Marketing team " +
        "|| WIKI PAGE: https://domain.org/wiki/Marketing " +
        "|| MAIL iki/Marketing " +
        "|| MAI iki/Marketing " +
        "|| MAI iki/Marketing...",
    memberCount = 32,
    isEncrypted = true,
//    eventSink = {}
)
