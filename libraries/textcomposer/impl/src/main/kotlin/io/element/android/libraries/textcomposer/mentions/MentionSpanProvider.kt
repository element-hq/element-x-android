/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.mentions

import io.element.android.libraries.matrix.api.core.EventId
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
    private val mentionSpanFormatter: MentionSpanFormatter,
) {
    /**
     * Creates a mention span from a text and URL.
     *
     * @param text The display text for the mention
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
     * @param text The display text for the mention
     * @param permalinkData The permalink data associated with the mention
     * @return A mention span based on the permalink data, null if the permalink data is not supported
     */
    fun getMentionSpanFor(text: String, permalinkData: PermalinkData): MentionSpan? {
        return when (permalinkData) {
            is PermalinkData.UserLink -> {
                createUserMentionSpan(text, permalinkData.userId)
            }
            is PermalinkData.RoomLink -> {
                val eventId = permalinkData.eventId
                if (eventId != null) {
                    createMessageMentionSpan(text, permalinkData.roomIdOrAlias, eventId)
                } else {
                    createRoomMentionSpan(text, permalinkData.roomIdOrAlias)
                }
            }
            else -> null
        }
    }

    /**
     * Create a mention span for a user mention.
     *
     * @param displayName The display name for the user
     * @param userId The user ID
     * @return A mention span for the user
     */
    fun createUserMentionSpan(displayName: String, userId: UserId): MentionSpan {
        return MentionSpan(
            originalText = displayName,
            type = MentionType.User(userId = userId)
        ).apply {
            updateDisplayText(mentionSpanFormatter)
        }
    }

    /**
     * Create a mention span for a room mention.
     *
     * @param roomName The display name for the room
     * @param roomIdOrAlias The room ID or alias
     * @return A mention span for the room
     */
    fun createRoomMentionSpan(roomName: String, roomIdOrAlias: RoomIdOrAlias): MentionSpan {
        return MentionSpan(
            originalText = roomName,
            type = MentionType.Room(roomIdOrAlias)
        ).apply {
            updateDisplayText(mentionSpanFormatter)
        }
    }

    /**
     * Create a mention span for a message mention.
     *
     * @param displayText The display text for the message
     * @param roomIdOrAlias The room ID or alias where the message is located
     * @param eventId The event ID of the message
     * @param currentRoomId Optional current room ID for context
     * @return A mention span for the message
     */
    fun createMessageMentionSpan(
        displayText: String,
        roomIdOrAlias: RoomIdOrAlias,
        eventId: EventId,
    ): MentionSpan {
        return MentionSpan(
            originalText = displayText,
            type = MentionType.Message(roomIdOrAlias, eventId)
        ).apply {
            updateDisplayText(mentionSpanFormatter)
        }
    }

    /**
     * Create a mention span for @room (everyone).
     *
     * @return A mention span for @room
     */
    fun createEveryoneMentionSpan(): MentionSpan {
        return MentionSpan(
            originalText = "@room",
            type = MentionType.Everyone
        ).also { span ->
            span.updateDisplayText(mentionSpanFormatter)
        }
    }
}
