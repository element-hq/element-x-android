/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.util

import io.element.android.libraries.matrix.api.room.IntentionalMention
import io.element.android.libraries.matrix.api.timeline.MsgType
import io.element.android.libraries.matrix.impl.room.map
import org.matrix.rustcomponents.sdk.MessageContent
import org.matrix.rustcomponents.sdk.MessageType
import org.matrix.rustcomponents.sdk.RoomMessageEventContentWithoutRelation
import org.matrix.rustcomponents.sdk.TextMessageContent
import org.matrix.rustcomponents.sdk.contentWithoutRelationFromMessage
import org.matrix.rustcomponents.sdk.messageEventContentFromHtml
import org.matrix.rustcomponents.sdk.messageEventContentFromHtmlAsEmote
import org.matrix.rustcomponents.sdk.messageEventContentFromMarkdown
import org.matrix.rustcomponents.sdk.messageEventContentFromMarkdownAsEmote

/**
 * Creates a [RoomMessageEventContentWithoutRelation] from a body, an html body and a list of mentions.
 */
object MessageEventContent {
    fun from(
        body: String,
        htmlBody: String?,
        intentionalMentions: List<IntentionalMention>,
        msgType: MsgType = MsgType.MSG_TYPE_TEXT,
        asPlainText: Boolean = false,
    ): RoomMessageEventContentWithoutRelation {
        return when {
            asPlainText -> contentWithoutRelationFromMessage(
                MessageContent(
                    msgType = MessageType.Text(
                        TextMessageContent(
                            body = body,
                            formatted = null,
                        )
                    ),
                    body = body,
                    isEdited = false,
                    mentions = null,
                )
            )
            htmlBody != null -> if (msgType == MsgType.MSG_TYPE_EMOTE) {
                messageEventContentFromHtmlAsEmote(body, htmlBody)
            } else {
                messageEventContentFromHtml(body, htmlBody)
            }
            else -> if (msgType == MsgType.MSG_TYPE_EMOTE) {
                messageEventContentFromMarkdownAsEmote(body)
            } else {
                messageEventContentFromMarkdown(body)
            }
        }
            .withMentions(intentionalMentions.map())
    }
}
