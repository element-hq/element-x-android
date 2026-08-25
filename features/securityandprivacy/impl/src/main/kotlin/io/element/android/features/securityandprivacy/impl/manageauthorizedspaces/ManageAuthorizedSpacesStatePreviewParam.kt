/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.manageauthorizedspaces

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.previewutils.room.aSpaceRoom
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet

open class ManageAuthorizedSpacesStatePreviewParam : PreviewParameterProvider<ManageAuthorizedSpacesState> {
    override val values: Sequence<ManageAuthorizedSpacesState>
        get() = sequenceOf(
            aManageAuthorizedSpacesState(),
            aManageAuthorizedSpacesState(
                unknownSpaceIds = listOf(aRoomId(99))
            ),
            aManageAuthorizedSpacesState(
                selectedIds = listOf(aRoomId(1), aRoomId(3)),
            ),
        )
}

private fun aRoomId(index: Int) = RoomId("!roomId$index:matrix.org")

private fun aSpaceRoomList(count: Int): List<SpaceRoom> {
    return (1..count).map { index ->
        aSpaceRoom(
            roomId = aRoomId(index),
            displayName = "Space $index",
            canonicalAlias = if (index % 2 == 0) {
                RoomAlias("#space$index:matrix.org")
            } else {
                null
            }
        )
    }
}

fun aManageAuthorizedSpacesState(
    selectableSpaces: List<SpaceRoom> = aSpaceRoomList(5),
    unknownSpaceIds: List<RoomId> = emptyList(),
    selectedIds: List<RoomId> = emptyList(),
    eventSink: (ManageAuthorizedSpacesEvent) -> Unit = {},
) = ManageAuthorizedSpacesState(
    selectableSpaces = selectableSpaces.toImmutableSet(),
    unknownSpaceIds = unknownSpaceIds.toImmutableList(),
    selectedIds = selectedIds.toImmutableList(),
    eventSink = eventSink,
)
