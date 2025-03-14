/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.utils

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.util.Patterns
import androidx.core.text.getSpans
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.RoomScope
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

interface TextPillificationHelper {
    fun pillify(text: CharSequence, pillifyMatrixPatterns: Boolean = true): CharSequence
}

@ContributesBinding(RoomScope::class)
class DefaultTextPillificationHelper @Inject constructor(
    private val mentionSpanProvider: MentionSpanProvider,
    private val permalinkBuilder: PermalinkBuilder,
    private val permalinkParser: PermalinkParser,
    private val roomMemberProfilesCache: RoomMemberProfilesCache,
) : TextPillificationHelper {
    @Suppress("LoopWithTooManyJumpStatements")
    override fun pillify(text: CharSequence, pillifyMatrixPatterns: Boolean): CharSequence {
        return SpannableStringBuilder(text).apply {
            if (pillifyMatrixPatterns) {
                pillifyMatrixPatterns(this)
            }
            pillifyPermalinks(this)
        }
    }

    private fun pillifyMatrixPatterns(text: SpannableStringBuilder) {
        val matches = MatrixPatterns.findPatterns(text, permalinkParser).sortedByDescending { it.end }
        if (matches.isEmpty()) return
        for (match in matches) {
            when (match.type) {
                MatrixPatternType.USER_ID -> {
                    val mentionSpanExists = text.getSpans<MentionSpan>(match.start, match.end).isNotEmpty()
                    if (!mentionSpanExists) {
                        val userId = UserId(match.value)
                        val permalink = permalinkBuilder.permalinkForUser(userId).getOrNull() ?: continue
                        val mentionSpan = mentionSpanProvider.getMentionSpanFor(match.value, permalink)
                        if (mentionSpan != null) {
                            roomMemberProfilesCache.getDisplayName(userId)?.let { mentionSpan.text = it }
                            text.replace(match.start, match.end, "@ ")
                            text.setSpan(mentionSpan, match.start, match.start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                }
                MatrixPatternType.ROOM_ALIAS -> {
                    val mentionSpanExists = text.getSpans<MentionSpan>(match.start, match.end).isNotEmpty()
                    if (!mentionSpanExists) {
                        val permalink = permalinkBuilder.permalinkForRoomAlias(RoomAlias(match.value)).getOrNull() ?: continue
                        val mentionSpan = mentionSpanProvider.getMentionSpanFor(match.value, permalink)
                        if (mentionSpan != null) {
                            text.replace(match.start, match.end, "@ ")
                            text.setSpan(mentionSpan, match.start, match.start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                }
                MatrixPatternType.AT_ROOM -> {
                    val mentionSpanExists = text.getSpans<MentionSpan>(match.start, match.end).isNotEmpty()
                    if (!mentionSpanExists) {
                        val mentionSpan = mentionSpanProvider.getMentionSpanFor("@room", "")
                        if (mentionSpan != null) {
                            text.replace(match.start, match.end, "@ ")
                            text.setSpan(mentionSpan, match.start, match.start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                }
                else -> Unit
            }
        }
    }

    private fun pillifyPermalinks(text: SpannableStringBuilder) {
        for (match in Patterns.WEB_URL.toRegex().findAll(text)) {
            val start = match.range.first
            val end = match.range.last + 1
            val mentionSpanExists = text.getSpans<MentionSpan>(start, end).isNotEmpty()
            if (!mentionSpanExists) {
                val url = text.substring(match.range)
                val mentionSpan = mentionSpanProvider.getMentionSpanFor(match.value, url)
                if (mentionSpan != null) {
                    text.setSpan(mentionSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
    }
}
