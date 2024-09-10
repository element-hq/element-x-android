/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomaliasresolver.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.room.alias.ResolvedRoomAlias

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
    resolveState: AsyncData<ResolvedRoomAlias> = AsyncData.Uninitialized,
    eventSink: (RoomAliasResolverEvents) -> Unit = {}
) = RoomAliasResolverState(
    roomAlias = roomAlias,
    resolveState = resolveState,
    eventSink = eventSink,
)

private val A_ROOM_ALIAS = RoomAlias("#exa:matrix.org")
