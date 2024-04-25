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

package io.element.android.features.roomlist.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.RoomId

open class RoomListStateContextMenuShownProvider : PreviewParameterProvider<RoomListState.ContextMenu.Shown> {
    override val values: Sequence<RoomListState.ContextMenu.Shown>
        get() = sequenceOf(
            aContextMenuShown(hasNewContent = true),
            aContextMenuShown(isDm = true),
            aContextMenuShown(roomName = null)
        )
}

internal fun aContextMenuShown(
    roomName: String? = "aRoom",
    isDm: Boolean = false,
    hasNewContent: Boolean = false,
    isFavorite: Boolean = false,
) = RoomListState.ContextMenu.Shown(
    roomId = RoomId("!aRoom:aDomain"),
    roomName = roomName,
    isDm = isDm,
    markAsUnreadFeatureFlagEnabled = true,
    hasNewContent = hasNewContent,
    isFavorite = isFavorite,
)
