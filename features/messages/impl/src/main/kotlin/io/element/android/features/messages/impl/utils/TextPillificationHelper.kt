/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.utils

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.URLSpan
import android.util.Patterns
import androidx.core.text.getSpans
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.MatrixPatternType
import io.element.android.libraries.matrix.api.core.MatrixPatterns
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.textcomposer.mentions.MentionSpanProvider
import io.element.android.libraries.textcomposer.mentions.getMentionSpans
import io.element.android.wysiwyg.view.spans.CodeBlockSpan
import io.element.android.wysiwyg.view.spans.InlineCodeSpan

interface TextPillificationHelper {
    fun pillify(text: CharSequence, pillifyPermalinks: Boolean = true): CharSequence
}

@ContributesBinding(RoomScope::class)
class DefaultTextPillificationHelper(
    private val mentionSpanProvider: MentionSpanProvider,
    private val permalinkParser: PermalinkParser,
    private val permalinkBuilder: PermalinkBuilder,
) : TextPillificationHelper {
    @Suppress("LoopWithTooManyJumpStatements")
    override fun pillify(text: CharSequence, pillifyPermalinks: Boolean): CharSequence {
        return SpannableStringBuilder(text).apply {
            pillifyMatrixPatterns(this)
            if (pillifyPermalinks) {
                pillifyPermalinks(this)
            }
        }
    }

    private fun pillifyMatrixPatterns(text: SpannableStringBuilder) {
        val matches = MatrixPatterns.findPatterns(text, permalinkParser).sortedByDescending { it.end }
        if (matches.isEmpty()) return
        for (match in matches) {
            if (!text.canPillify(match.start, match.end)) continue
            when (match.type) {
                MatrixPatternType.USER_ID -> {
                    val userId = UserId(match.value)
                    val mentionSpan = mentionSpanProvider.createUserMentionSpan(userId)
                    text.replace(match.start, match.end, "@ ")
                    text.setSpan(mentionSpan, match.start, match.start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    permalinkBuilder.permalinkForUser(userId).getOrNull()?.also { permalink ->
                        // Also add a URLSpan in case of raw user id so it can be clicked
                        val urlSpan = URLSpan(permalink)
                        text.setSpan(urlSpan, match.start, match.start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
                MatrixPatternType.ROOM_ALIAS -> {
                    val roomAlias = RoomAlias(match.value)
                    val mentionSpan = mentionSpanProvider.createRoomMentionSpan(roomAlias.toRoomIdOrAlias())
                    text.replace(match.start, match.end, "@ ")
                    text.setSpan(mentionSpan, match.start, match.start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    permalinkBuilder.permalinkForRoomAlias(roomAlias).getOrNull()?.also { permalink ->
                        // Also add a URLSpan in case of raw room alias so it can be clicked
                        val urlSpan = URLSpan(permalink)
                        text.setSpan(urlSpan, match.start, match.start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
                MatrixPatternType.AT_ROOM -> {
                    val mentionSpan = mentionSpanProvider.createEveryoneMentionSpan()
                    text.replace(match.start, match.end, "@ ")
                    text.setSpan(mentionSpan, match.start, match.start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                else -> Unit
            }
        }
    }

    private fun pillifyPermalinks(text: SpannableStringBuilder) {
        for (match in Patterns.WEB_URL.toRegex().findAll(text)) {
            val start = match.range.first
            val end = match.range.last + 1
            if (!text.canPillify(start, end)) continue
            val url = text.substring(match.range)
            val mentionSpan = mentionSpanProvider.getMentionSpanFor(match.value, url)
            if (mentionSpan != null) {
                text.setSpan(mentionSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    private fun Spanned.canPillify(start: Int, end: Int): Boolean {
        if (getMentionSpans(start, end).isNotEmpty()) return false
        if (getSpans<CodeBlockSpan>(start, end).isNotEmpty()) return false
        if (getSpans<InlineCodeSpan>(start, end).isNotEmpty()) return false
        return true
    }
}
