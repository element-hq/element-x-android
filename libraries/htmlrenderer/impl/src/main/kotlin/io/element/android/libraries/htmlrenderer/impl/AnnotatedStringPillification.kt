/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.impl

import android.util.Patterns
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import io.element.android.libraries.htmlrenderer.api.HtmlMessageParser.Companion.INLINE_CODE_ANNOTATION_TAG
import io.element.android.libraries.htmlrenderer.api.HtmlMessageParser.Companion.LINK_ANNOTATION_TAG
import io.element.android.libraries.htmlrenderer.api.MentionNodeContent
import io.element.android.libraries.matrix.api.core.MatrixPatternType
import io.element.android.libraries.matrix.api.core.MatrixPatterns
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap

private val MATRIX_URI_REGEX = Regex("""matrix:(?:u|user|r|room|roomid|e|event)/\S*[^\s.,;:!?)]""", RegexOption.IGNORE_CASE)
private val WEB_URL_REGEX = Patterns.WEB_URL.toRegex()
private val TRAILING_PUNCTUATION = charArrayOf('.', ',', ';', ':', '!', '?', '…')

private const val PILL_ID_PREFIX = "autopill_"

/**
 * The result of [pillify]: the new [text] and the full map of inline content placeholders in it.
 */
internal data class PillifiedText(
    val text: AnnotatedString,
    val inlineContent: ImmutableMap<String, MentionNodeContent>,
)

/**
 * Replaces the mentions found in the raw text with inline content placeholders (pills), similar to what
 * [io.element.android.libraries.designsystem.components.linkify] does for links. It detects:
 *
 * - User ids (`@user:server.tld`) -> [MentionNodeContent.User].
 * - Room aliases (`#room:server.tld`) -> [MentionNodeContent.Room].
 * - `@room` -> [MentionNodeContent.Everyone].
 * - User and room permalinks (`https://matrix.to/#/@user:server.tld`, `matrix:u/user:server.tld`, ...).
 *
 * Text that is already a mention placeholder (from [inlineContent]), a link or inline code is left untouched.
 *
 * @param inlineContent the inline content placeholders already present in this [AnnotatedString].
 * @param permalinkParser used to parse permalinks found in the text.
 * @param permalinkBuilder used to build permalinks for the detected mentions if they weren't in a URL format.
 * @return the new text, and [inlineContent] plus any added pill.
 */
internal fun AnnotatedString.pillify(
    inlineContent: Map<String, MentionNodeContent>,
    permalinkParser: PermalinkParser,
    permalinkBuilder: PermalinkBuilder,
): PillifiedText {
    val matches = findPillMatches(inlineContent.keys, permalinkParser, permalinkBuilder)
    if (matches.isEmpty()) return PillifiedText(this, inlineContent.toImmutableMap())

    val allInlineContent = inlineContent.toMutableMap()
    var pillCount = 0
    val text = buildAnnotatedString {
        var cursor = 0
        for (match in matches) {
            append(this@pillify.subSequence(cursor, match.start))
            var id: String
            do {
                id = "$PILL_ID_PREFIX${pillCount++}"
            } while (id in allInlineContent)
            allInlineContent[id] = match.mention
            appendInlineContent(id, match.mention.displayText)
            cursor = match.end
        }
        append(this@pillify.subSequence(cursor, this@pillify.length))
    }
    return PillifiedText(text, allInlineContent.toImmutableMap())
}

private data class PillMatch(
    val start: Int,
    val end: Int,
    val mention: MentionNodeContent,
)

/**
 * Finds the ranges to pillify, sorted by position and without overlaps: when two candidates overlap, the longest one wins
 * (i.e. `@room:server.tld` is a user id, not an `@room` mention).
 */
private fun AnnotatedString.findPillMatches(
    inlineContentIds: Set<String>,
    permalinkParser: PermalinkParser,
    permalinkBuilder: PermalinkBuilder,
): List<PillMatch> {
    val candidates = buildList {
        addAll(findMatrixPatternMatches(permalinkParser, permalinkBuilder))
        addAll(findPermalinkMatches(MATRIX_URI_REGEX, inlineContentIds, permalinkParser, permalinkBuilder))
        addAll(findPermalinkMatches(WEB_URL_REGEX, inlineContentIds, permalinkParser, permalinkBuilder))
    }
    val sortedCandidates = candidates
        .filter { canPillify(it.start, it.end, inlineContentIds) }
        .sortedWith(compareBy<PillMatch> { it.start }.thenByDescending { it.end - it.start })
    val result = mutableListOf<PillMatch>()
    for (candidate in sortedCandidates) {
        val overlapsPrevious = result.lastOrNull()?.let { candidate.start < it.end } == true
        if (!overlapsPrevious) result.add(candidate)
    }
    return result
}

private fun AnnotatedString.findMatrixPatternMatches(permalinkParser: PermalinkParser, permalinkBuilder: PermalinkBuilder): List<PillMatch> {
    return MatrixPatterns.findPatterns(text, permalinkParser).mapNotNull { match ->
        val mention = when (match.type) {
            MatrixPatternType.USER_ID -> {
                val userId = UserId(match.value)
                permalinkBuilder.permalinkForUser(userId).getOrNull()?.let {
                    MentionNodeContent.User(displayText = match.value, userId = userId, permalinkUrl = it)
                }
            }
            MatrixPatternType.ROOM_ALIAS -> {
                val roomAlias = RoomAlias(match.value)
                permalinkBuilder.permalinkForRoomAlias(roomAlias).getOrNull()?.let {
                    MentionNodeContent.Room(displayText = match.value, roomIdOrAlias = roomAlias.toRoomIdOrAlias(), permalinkUrl = it)
                }
            }
            MatrixPatternType.AT_ROOM -> MentionNodeContent.Everyone(displayText = match.value)
            else -> null
        }
        mention?.let { PillMatch(match.start, match.end, it) }
    }
}

private fun AnnotatedString.findPermalinkMatches(
    regex: Regex,
    inlineContentIds: Set<String>,
    permalinkParser: PermalinkParser,
    permalinkBuilder: PermalinkBuilder,
): List<PillMatch> {
    return regex.findAll(text).mapNotNull { match ->
        val start = match.range.first
        val end = trimTrailingPunctuation(start, match.range.last + 1)
        // Check this before parsing the url, as links will be discarded anyway
        if (end <= start || !canPillify(start, end, inlineContentIds)) return@mapNotNull null
        val mention = when (val permalink = permalinkParser.parse(text.substring(start, end))) {
            is PermalinkData.UserLink -> permalinkBuilder.permalinkForUser(permalink.userId).getOrNull()?.let {
                MentionNodeContent.User(
                    displayText = permalink.userId.value,
                    userId = permalink.userId,
                    permalinkUrl = it,
                )
            }
            // A link to a specific event is a regular link, not a mention pill.
            is PermalinkData.RoomLink -> if (permalink.eventId == null && permalink.roomIdOrAlias is RoomIdOrAlias.Alias) {
                permalinkBuilder.permalinkForRoomAlias((permalink.roomIdOrAlias as RoomIdOrAlias.Alias).roomAlias).getOrNull()?.let {
                    MentionNodeContent.Room(
                        displayText = permalink.roomIdOrAlias.identifier,
                        roomIdOrAlias = permalink.roomIdOrAlias,
                        permalinkUrl = it,
                    )
                }
            } else {
                null
            }
            else -> null
        }
        mention?.let { PillMatch(start, end, it) }
    }.toList()
}

/**
 * Returns the end index of the url in [start, end) once any trailing punctuation or unbalanced closing parenthesis is removed.
 */
private fun AnnotatedString.trimTrailingPunctuation(start: Int, end: Int): Int {
    var newEnd = end
    while (newEnd > start) {
        val char = text[newEnd - 1]
        val isUnbalancedParenthesis = char == ')' && text.substring(start, newEnd).let { it.count { c -> c == ')' } > it.count { c -> c == '(' } }
        if (char !in TRAILING_PUNCTUATION && !isUnbalancedParenthesis) break
        newEnd--
    }
    return newEnd
}

private fun AnnotatedString.canPillify(start: Int, end: Int, inlineContentIds: Set<String>): Boolean {
    if (hasStringAnnotations(LINK_ANNOTATION_TAG, start, end)) return false
    if (hasStringAnnotations(INLINE_CODE_ANNOTATION_TAG, start, end)) return false
    // Existing mentions: their placeholder text could contain a user id or room alias too
    if (getStringAnnotations(start, end).any { it.item in inlineContentIds }) return false
    return true
}
