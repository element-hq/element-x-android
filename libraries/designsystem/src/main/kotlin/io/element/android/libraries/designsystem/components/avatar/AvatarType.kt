/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
sealed interface AvatarType {
    data object User : AvatarType

    data class Room(
        val isTombstoned: Boolean = false,
        val heroes: ImmutableList<AvatarData> = persistentListOf(),
    ) : AvatarType

    data class Space(
        val cornerSize: Dp,
        val isTombstoned: Boolean = false,
    ) : AvatarType
}
