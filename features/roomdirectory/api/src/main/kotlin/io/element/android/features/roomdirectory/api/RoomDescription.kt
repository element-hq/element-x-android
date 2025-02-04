/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdirectory.api

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
@Immutable
data class RoomDescription(
    val roomId: RoomId,
    val name: String?,
    val alias: RoomAlias?,
    val topic: String?,
    val avatarUrl: String?,
    val joinRule: JoinRule,
    val numberOfMembers: Long,
) : Parcelable {
    enum class JoinRule {
        PUBLIC,
        KNOCK,
        UNKNOWN
    }

    @IgnoredOnParcel
    val computedName = name ?: alias?.value ?: roomId.value

    @IgnoredOnParcel
    val computedDescription: String
        get() {
            return when {
                topic != null -> topic
                name != null && alias != null -> alias.value
                name == null && alias == null -> ""
                else -> roomId.value
            }
        }

    @IgnoredOnParcel
    val canJoinOrKnock = joinRule == JoinRule.PUBLIC || joinRule == JoinRule.KNOCK

    fun avatarData(size: AvatarSize) = AvatarData(
        id = roomId.value,
        name = name,
        url = avatarUrl,
        size = size,
    )
}
