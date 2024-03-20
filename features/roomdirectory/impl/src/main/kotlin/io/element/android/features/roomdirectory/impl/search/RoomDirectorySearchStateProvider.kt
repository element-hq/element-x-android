/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.roomdirectory.impl.search

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.roomdirectory.impl.search.model.RoomDirectorySearchResult
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

open class RoomDirectorySearchStateProvider : PreviewParameterProvider<RoomDirectorySearchState> {
    override val values: Sequence<RoomDirectorySearchState>
        get() = sequenceOf(
            aRoomDirectorySearchState(),
            aRoomDirectorySearchState(
                query = "Element",
                results = persistentListOf(
                    RoomDirectorySearchResult(
                        roomId = RoomId("@exa:matrix.org"),
                        name = "Element X Android",
                        description = "Element X is a secure, private and decentralized messenger.",
                        avatarData = AvatarData(
                            id = "@exa:matrix.org",
                            name = "Element X Android",
                            url = null,
                            size = AvatarSize.RoomDirectorySearchItem
                        ),
                        canBeJoined = true,
                    ),
                    RoomDirectorySearchResult(
                        roomId = RoomId("@exi:matrix.org"),
                        name = "Element X iOS",
                        description = "Element X is a secure, private and decentralized messenger.",
                        avatarData = AvatarData(
                            id = "@exi:matrix.org",
                            name = "Element X iOS",
                            url = null,
                            size = AvatarSize.RoomDirectorySearchItem
                        ),
                        canBeJoined = false,
                    )
                )
            ),
        )
}

fun aRoomDirectorySearchState(
    query: String = "",
    results: ImmutableList<RoomDirectorySearchResult> = persistentListOf(),
) = RoomDirectorySearchState(
    query = query,
    results = results,
    eventSink = { }
)
