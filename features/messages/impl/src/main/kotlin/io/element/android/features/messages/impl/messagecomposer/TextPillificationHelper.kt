/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.messagecomposer

import android.text.Spannable
import android.text.SpannableStringBuilder
import androidx.core.text.getSpans
import io.element.android.libraries.matrix.api.core.MatrixPatternType
import io.element.android.libraries.matrix.api.core.MatrixPatterns
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.ui.messages.RoomMemberProfilesCache
import io.element.android.libraries.textcomposer.mentions.MentionSpan
import io.element.android.libraries.textcomposer.mentions.MentionSpanProvider
import javax.inject.Inject

class TextPillificationHelper @Inject constructor(
    private val mentionSpanProvider: MentionSpanProvider,
    private val permalinkBuilder: PermalinkBuilder,
    private val permalinkParser: PermalinkParser,
    private val roomMemberProfilesCache: RoomMemberProfilesCache,
) {
    @Suppress("LoopWithTooManyJumpStatements")
    fun pillify(text: CharSequence): CharSequence {
        val matches = MatrixPatterns.findPatterns(text, permalinkParser).sortedByDescending { it.end }
        if (matches.isEmpty()) return text

        val spannable = SpannableStringBuilder(text)
        for (match in matches) {
            when (match.type) {
                MatrixPatternType.USER_ID -> {
                    val mentionSpanExists = spannable.getSpans<MentionSpan>(match.start, match.end).isNotEmpty()
                    if (!mentionSpanExists) {
                        val userId = UserId(match.value)
                        val permalink = permalinkBuilder.permalinkForUser(userId).getOrNull() ?: continue
                        val mentionSpan = mentionSpanProvider.getMentionSpanFor(match.value, permalink)
                        roomMemberProfilesCache.getDisplayName(userId)?.let { mentionSpan.text = it }
                        spannable.replace(match.start, match.end + 1, "@ ")
                        spannable.setSpan(mentionSpan, match.start, match.start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
                MatrixPatternType.ROOM_ALIAS -> {
                    val mentionSpanExists = spannable.getSpans<MentionSpan>(match.start, match.end).isNotEmpty()
                    if (!mentionSpanExists) {
                        val permalink = permalinkBuilder.permalinkForRoomAlias(RoomAlias(match.value)).getOrNull() ?: continue
                        val mentionSpan = mentionSpanProvider.getMentionSpanFor(match.value, permalink)
                        spannable.replace(match.start, match.end + 1, "@ ")
                        spannable.setSpan(mentionSpan, match.start, match.start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
                MatrixPatternType.AT_ROOM -> {
                    val mentionSpanExists = spannable.getSpans<MentionSpan>(match.start, match.end).isNotEmpty()
                    if (!mentionSpanExists) {
                        val mentionSpan = mentionSpanProvider.getMentionSpanFor("@room", "")
                        spannable.replace(match.start, match.end + 1, "@ ")
                        spannable.setSpan(mentionSpan, match.start, match.start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
                else -> Unit
            }
        }
        return spannable
    }
}
