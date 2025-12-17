/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.mentions

import dev.zacsweers.metro.Inject
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser

private const val EVERYONE_MENTION_TEXT = "@room"

/**
 * Provider for [MentionSpan]s.
 */
@Inject open class MentionSpanProvider(
    private val permalinkParser: PermalinkParser,
    private val mentionSpanFormatter: MentionSpanFormatter,
    private val mentionSpanTheme: MentionSpanTheme,
) {
    /**
     * Creates a mention span from a text and URL.
     *
     * @param text The text associated with the mention
     * @param url The URL associated with the mention
     * @return A mention span if the URL can be parsed as a permalink, null otherwise
     */
    fun getMentionSpanFor(text: String, url: String): MentionSpan? {
        val permalinkData = permalinkParser.parse(url)
        return getMentionSpanFor(text, permalinkData)
    }

    /**
     * Creates a mention span from a text and permalink data.
     *
     * @param text The text associated with the mention
     * @param permalinkData The permalink data associated with the mention
     * @return A mention span based on the permalink data, null if the permalink data is not supported
     */
    private fun getMentionSpanFor(text: String, permalinkData: PermalinkData): MentionSpan? {
        return when (permalinkData) {
            is PermalinkData.UserLink -> {
                createUserMentionSpan(permalinkData.userId)
            }
            is PermalinkData.RoomLink -> {
                val eventId = permalinkData.eventId
                if (eventId != null) {
                    createMessageMentionSpan(permalinkData.roomIdOrAlias, eventId)
                } else {
                    createRoomMentionSpan(permalinkData.roomIdOrAlias)
                }
            }
            is PermalinkData.FallbackLink -> {
                if (text == EVERYONE_MENTION_TEXT) {
                    createEveryoneMentionSpan()
                } else {
                    null
                }
            }
            else -> null
        }
    }

    /**
     * Create a mention span for a user mention.
     *
     * @param userId The user ID
     * @return A mention span for the user
     */
    fun createUserMentionSpan(userId: UserId): MentionSpan {
        return MentionSpan(type = MentionType.User(userId = userId)).apply {
            updateDisplayText(mentionSpanFormatter)
            updateTheme(mentionSpanTheme)
        }
    }

    /**
     * Create a mention span for a room mention.
     *
     * @param roomIdOrAlias The room ID or alias
     * @return A mention span for the room
     */
    fun createRoomMentionSpan(roomIdOrAlias: RoomIdOrAlias): MentionSpan {
        return MentionSpan(MentionType.Room(roomIdOrAlias)).apply {
            updateDisplayText(mentionSpanFormatter)
            updateTheme(mentionSpanTheme)
        }
    }

    /**
     * Create a mention span for a message mention.
     *
     * @param roomIdOrAlias The room ID or alias where the message is located
     * @param eventId The event ID of the message
     * @return A mention span for the message
     */
    fun createMessageMentionSpan(
        roomIdOrAlias: RoomIdOrAlias,
        eventId: EventId,
    ): MentionSpan {
        return MentionSpan(type = MentionType.Message(roomIdOrAlias, eventId)).apply {
            updateTheme(mentionSpanTheme)
            updateDisplayText(mentionSpanFormatter)
        }
    }

    /**
     * Create a mention span for @room (everyone).
     *
     * @return A mention span for @room
     */
    fun createEveryoneMentionSpan(): MentionSpan {
        return MentionSpan(type = MentionType.Everyone).apply {
            updateTheme(mentionSpanTheme)
            updateDisplayText(mentionSpanFormatter)
        }
    }
}
