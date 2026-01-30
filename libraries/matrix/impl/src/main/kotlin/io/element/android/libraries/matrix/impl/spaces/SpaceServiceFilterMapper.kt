/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.spaces

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.spaces.SpaceServiceFilter
import org.matrix.rustcomponents.sdk.SpaceFilter as RustSpaceFilter

class SpaceServiceFilterMapper(
    private val spaceRoomMapper: SpaceRoomMapper,
) {
    fun map(spaceFilter: RustSpaceFilter): SpaceServiceFilter {
        return SpaceServiceFilter(
            spaceRoom = spaceRoomMapper.map(spaceFilter.spaceRoom),
            level = spaceFilter.level.toInt(),
            descendants = spaceFilter.descendants.map { RoomId(it) },
        )
    }
}
