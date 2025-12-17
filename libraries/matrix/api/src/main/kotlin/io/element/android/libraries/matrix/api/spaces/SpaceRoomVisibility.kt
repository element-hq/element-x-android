/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.spaces

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.room.join.JoinRule
@Immutable
sealed interface SpaceRoomVisibility {
    data object Private : SpaceRoomVisibility
    data object Public : SpaceRoomVisibility
    data object Restricted : SpaceRoomVisibility

    companion object {
        fun fromJoinRule(joinRule: JoinRule?): SpaceRoomVisibility = when (joinRule) {
            JoinRule.Public -> Public
            is JoinRule.Restricted, is JoinRule.KnockRestricted -> Restricted
            // Else fallback to Private
            else -> Private
        }
    }
}
