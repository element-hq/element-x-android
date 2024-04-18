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

package io.element.android.features.roomaliasresolver.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId

open class RoomAliasResolverStateProvider : PreviewParameterProvider<RoomAliasResolverState> {
    override val values: Sequence<RoomAliasResolverState>
        get() = sequenceOf(
            aRoomAliasResolverState(),
            aRoomAliasResolverState(
                resolveState = AsyncData.Loading(),
            ),
            aRoomAliasResolverState(
                resolveState = AsyncData.Failure(Exception("Error")),
            ),
        )
}

fun aRoomAliasResolverState(
    roomAlias: RoomAlias = A_ROOM_ALIAS,
    resolveState: AsyncData<RoomId> = AsyncData.Uninitialized,
    eventSink: (RoomAliasResolverEvents) -> Unit = {}
) = RoomAliasResolverState(
    roomAlias = roomAlias,
    resolveState = resolveState,
    eventSink = eventSink,
)

private val A_ROOM_ALIAS = RoomAlias("#exa:matrix.org")
