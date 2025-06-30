/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.powerlevels

import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.collections.immutable.ImmutableMap

data class RoomPowerLevels(
    val values: RoomPowerLevelsValues,
    val users: ImmutableMap<UserId, Long>,
)
