/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.preview

import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomType
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.user.MatrixUser

data class RoomPreviewInfo(
    /** The room id for this room. */
    val roomId: RoomId,
    /** The canonical alias for the room. */
    val canonicalAlias: RoomAlias?,
    /** The room's name, if set. */
    val name: String?,
    /** The room's topic, if set. */
    val topic: String?,
    /** The MXC URI to the room's avatar, if set. */
    val avatarUrl: String?,
    /** The number of joined members. */
    val numberOfJoinedMembers: Long,
    /** The number of active members (joined + invited), if known. */
    val numberOfActiveMembers: Long?,
    /** The room type (space, custom) or nothing, if it's a regular room. */
    val roomType: RoomType,
    /** Is the history world-readable for this room? */
    val isHistoryWorldReadable: Boolean,
    /** the membership of the current user. */
    val membership: CurrentUserMembership?,
    /** The room's join rule. */
    val joinRule: JoinRule?,
    /** Whether the room is direct or not, if known. */
    val isDirect: Boolean?,
    /** The room heroes, can be empty if unknown. */
    val heroes: List<MatrixUser>,
) {
    /**
     * Whether the room should be presented as a DM: a direct room with at most 2 active members.
     */
    val isDm: Boolean
        get() = isDirect == true && (numberOfActiveMembers ?: numberOfJoinedMembers) <= 2
}
