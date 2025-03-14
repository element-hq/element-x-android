/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.mentions

import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import javax.inject.Inject

/**
 * Provider for [MentionSpan]s.
 */
open class MentionSpanProvider @Inject constructor(
    private val permalinkParser: PermalinkParser,
) {
    fun getMentionSpanFor(text: String, url: String): MentionSpan? {
        val permalinkData = permalinkParser.parse(url)
        return getMentionSpanFor(text, permalinkData)
    }

    fun getMentionSpanFor(text: String, permalinkData: PermalinkData): MentionSpan? {
        return when (permalinkData) {
            is PermalinkData.UserLink -> {
                MentionSpan(
                    text = text,
                    type = MentionType.User(userId = permalinkData.userId)
                )
            }
            is PermalinkData.RoomLink -> {
                val eventId = permalinkData.eventId
                val mentionType = if (eventId != null) {
                    MentionType.Message(roomIdOrAlias = permalinkData.roomIdOrAlias, eventId = eventId)
                } else {
                    MentionType.Room(roomIdOrAlias = permalinkData.roomIdOrAlias)
                }
                MentionSpan(
                    text = text,
                    type = mentionType
                )
            }
            else -> null
        }
    }

    /**
     * Create a mention span for a user mention.
     */
    fun createUserMentionSpan(displayName: String, userId: UserId): MentionSpan {
        return MentionSpan(
            text = displayName,
            type = MentionType.User(userId = userId)
        )
    }

    /**
     * Create a mention span for a room mention.
     */
    fun createRoomMentionSpan(roomName: String, roomIdOrAlias: RoomIdOrAlias): MentionSpan {
        return MentionSpan(
            text = roomName,
            type = MentionType.Room(roomIdOrAlias)
        )
    }

    /**
     * Create a mention span for @room (everyone).
     */
    fun createEveryoneMentionSpan(): MentionSpan {
        return MentionSpan(
            text = "@room",
            type = MentionType.Everyone
        )
    }
}
