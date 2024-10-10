/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.mentions

import androidx.compose.runtime.Immutable
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.RoomMember

@Immutable
sealed interface ResolvedSuggestion {
    data object AtRoom : ResolvedSuggestion
    data class Member(val roomMember: RoomMember) : ResolvedSuggestion
    data class Alias(
        val roomAlias: RoomAlias,
        val roomId: RoomId,
        val roomName: String?,
        val roomAvatarUrl: String?,
    ) : ResolvedSuggestion {
        fun getAvatarData(size: AvatarSize) = AvatarData(
            id = roomId.value,
            name = roomName,
            url = roomAvatarUrl,
            size = size,
        )
    }
}
