/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.api

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

/**
 * The known display names of the users and rooms a [MentionNodeContent] can point to, used to replace
 * the raw handles in the mention pills with something more readable.
 *
 * @param users the display names of the room members, by user id.
 * @param rooms the room names, by room id or alias.
 */
@Immutable
data class MentionDisplayNames(
    val users: ImmutableMap<UserId, String> = persistentMapOf(),
    val rooms: ImmutableMap<RoomIdOrAlias, String> = persistentMapOf(),
) {
    /**
     * Returns the text to display in the pill for [mention]: the user or room display name if known,
     * or the original [MentionNodeContent.displayText] otherwise.
     */
    fun displayTextFor(mention: MentionNodeContent): String = when (mention) {
        is MentionNodeContent.User -> users[mention.userId]?.let { "@$it" }
        is MentionNodeContent.Room -> rooms[mention.roomIdOrAlias]?.let { "#$it" }
        is MentionNodeContent.Everyone -> null
    } ?: mention.displayText
}

/**
 * Provides the [MentionDisplayNames] used to render the mention pills. It's empty by default, so the
 * pills will display their original [MentionNodeContent.displayText].
 */
val LocalMentionDisplayNames = compositionLocalOf { MentionDisplayNames() }
