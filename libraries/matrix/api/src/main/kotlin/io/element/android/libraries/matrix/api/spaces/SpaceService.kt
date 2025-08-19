/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.spaces

import kotlinx.coroutines.flow.SharedFlow

interface SpaceService {
    val spaceRooms: SharedFlow<List<SpaceRoom>>
    suspend fun joinedSpaces(): Result<List<SpaceRoom>>
}
