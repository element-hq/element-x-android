/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.mentions

import androidx.compose.runtime.Stable
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import javax.inject.Inject

@Stable
open class MentionSpanProvider @Inject constructor(
    private val permalinkParser: PermalinkParser,
) {
    fun getMentionSpanFor(text: String, url: String): MentionSpan {
        val permalinkData = permalinkParser.parse(url)
        return when {
            permalinkData is PermalinkData.UserLink -> {
                MentionSpan(
                    text = text,
                    rawValue = permalinkData.userId.toString(),
                    type = MentionSpan.Type.USER,
                )
            }
            text == "@room" && permalinkData is PermalinkData.FallbackLink -> {
                MentionSpan(
                    text = text,
                    rawValue = "@room",
                    type = MentionSpan.Type.EVERYONE,
                )
            }
            permalinkData is PermalinkData.RoomLink -> {
                MentionSpan(
                    text = text,
                    rawValue = permalinkData.roomIdOrAlias.identifier,
                    type = MentionSpan.Type.ROOM,
                )
            }
            else -> {
                MentionSpan(
                    text = text,
                    rawValue = text,
                    type = MentionSpan.Type.ROOM,
                )
            }
        }
    }
}
