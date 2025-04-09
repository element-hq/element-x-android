/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.mentions

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.ui.messages.RoomMemberProfilesCache
import io.element.android.libraries.matrix.ui.messages.RoomNamesCache
import javax.inject.Inject

private const val EVERYONE_DISPLAY_TEXT = "@room"
private const val BUBBLE_ICON = "\uD83D\uDCAC" // 💬

interface MentionSpanFormatter {
    fun formatDisplayText(mentionType: MentionType): CharSequence
}

/**
 * Formatter for MentionSpan display text.
 * This class is responsible for formatting the display text of a MentionSpan
 * based on its MentionType and context.
 */
@ContributesBinding(RoomScope::class)
class DefaultMentionSpanFormatter @Inject constructor(
    private val roomMemberProfilesCache: RoomMemberProfilesCache,
    private val roomNamesCache: RoomNamesCache,
) : MentionSpanFormatter {
    /**
     * Format the display text for a mention span.
     *
     * @param mentionType The type of mention
     * @return The formatted display text
     */
    override fun formatDisplayText(mentionType: MentionType): CharSequence {
        return when (mentionType) {
            is MentionType.User -> formatUserMention(mentionType.userId)
            is MentionType.Room -> formatRoomMention(mentionType.roomIdOrAlias)
            is MentionType.Message -> formatMessageMention(mentionType.roomIdOrAlias)
            is MentionType.Everyone -> EVERYONE_DISPLAY_TEXT
        }
    }

    private fun formatUserMention(userId: UserId): String {
        // Try to get the display name from cache, fallback to userId
        val displayName = roomMemberProfilesCache.getDisplayName(userId)
        return if (displayName != null) {
            "@$displayName"
        } else {
            userId.value
        }
    }

    private fun formatRoomMention(roomIdOrAlias: RoomIdOrAlias): String {
        val displayName = roomNamesCache.getDisplayName(roomIdOrAlias)
        return if (displayName != null) {
            "#$displayName"
        } else {
            roomIdOrAlias.identifier
        }
    }

    private fun formatMessageMention(
        roomIdOrAlias: RoomIdOrAlias,
    ): String {
        val roomMention = formatRoomMention(roomIdOrAlias)
        return "$BUBBLE_ICON > $roomMention"
    }
}
