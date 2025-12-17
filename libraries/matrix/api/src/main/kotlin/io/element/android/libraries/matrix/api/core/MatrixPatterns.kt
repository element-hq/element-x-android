/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.core

import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser

/**
 * This class contains pattern to match the different Matrix ids
 * Ref: https://matrix.org/docs/spec/appendices#identifier-grammar
 */
object MatrixPatterns {
    // Note: TLD is not mandatory (localhost, IP address...)
    private const val DOMAIN_REGEX = ":[A-Za-z0-9.-]+(:[0-9]{2,5})?"

    private const val BASE_64_ALPHABET = "[0-9A-Za-z/\\+=]+"
    private const val BASE_64_URL_SAFE_ALPHABET = "[0-9A-Za-z/\\-_]+"

    // regex pattern to find matrix user ids in a string.
    // See https://matrix.org/docs/spec/appendices#historical-user-ids
    // Sadly, we need to relax the regex pattern a bit as there already exist some ids that don't match the spec.
    // Note: local part can be empty
    private const val MATRIX_USER_IDENTIFIER_REGEX = "^@\\S*?$DOMAIN_REGEX$"
    private val PATTERN_CONTAIN_MATRIX_USER_IDENTIFIER = MATRIX_USER_IDENTIFIER_REGEX.toRegex()

    // !localpart:domain" used in most room versions prior to MSC4291
    // Note: roomId can be arbitrary strings, including space and new line char
    private const val MATRIX_ROOM_IDENTIFIER_REGEX = "^!.+$DOMAIN_REGEX$"
    private val PATTERN_CONTAIN_MATRIX_ROOM_IDENTIFIER = MATRIX_ROOM_IDENTIFIER_REGEX.toRegex(RegexOption.DOT_MATCHES_ALL)

    // "!event_id_base_64" used in room versions post MSC4291
    private const val MATRIX_ROOM_IDENTIFIER_DOMAINLESS_REGEX = "!$BASE_64_URL_SAFE_ALPHABET"
    private val PATTERN_CONTAIN_MATRIX_ROOM_IDENTIFIER_DOMAINLESS = MATRIX_ROOM_IDENTIFIER_DOMAINLESS_REGEX.toRegex()

    // regex pattern to match room aliases.
    private const val MATRIX_ROOM_ALIAS_REGEX = "^#\\S+$DOMAIN_REGEX$"
    private val PATTERN_CONTAIN_MATRIX_ALIAS = MATRIX_ROOM_ALIAS_REGEX.toRegex(RegexOption.IGNORE_CASE)

    // regex pattern to match event ids.
    // Sadly, we need to relax the regex pattern a bit as there already exist some ids that don't match the spec.
    // v1 and v2: arbitrary string + domain
    private const val MATRIX_EVENT_IDENTIFIER_REGEX = "^\\$.+$DOMAIN_REGEX$"
    private val PATTERN_CONTAIN_MATRIX_EVENT_IDENTIFIER = MATRIX_EVENT_IDENTIFIER_REGEX.toRegex()

    // v3: base64
    private const val MATRIX_EVENT_IDENTIFIER_V3_REGEX = "\\$$BASE_64_ALPHABET"
    private val PATTERN_CONTAIN_MATRIX_EVENT_IDENTIFIER_V3 = MATRIX_EVENT_IDENTIFIER_V3_REGEX.toRegex()

    // v4: url-safe base64
    private const val MATRIX_EVENT_IDENTIFIER_V4_REGEX = "\\$$BASE_64_URL_SAFE_ALPHABET"
    private val PATTERN_CONTAIN_MATRIX_EVENT_IDENTIFIER_V4 = MATRIX_EVENT_IDENTIFIER_V4_REGEX.toRegex()

    private const val MAX_IDENTIFIER_LENGTH = 255

    /**
     * Tells if a string is a valid user Id.
     *
     * @param str the string to test
     * @return true if the string is a valid user id
     */
    fun isUserId(str: String?): Boolean {
        return str != null &&
            str.length <= MAX_IDENTIFIER_LENGTH &&
            str matches PATTERN_CONTAIN_MATRIX_USER_IDENTIFIER
    }

    /**
     * Tells if a string is a valid room id.
     *
     * @param str the string to test
     * @return true if the string is a valid room Id
     */
    fun isRoomId(str: String?): Boolean {
        return str != null &&
            str.length <= MAX_IDENTIFIER_LENGTH &&
            (str matches PATTERN_CONTAIN_MATRIX_ROOM_IDENTIFIER_DOMAINLESS ||
                str matches PATTERN_CONTAIN_MATRIX_ROOM_IDENTIFIER)
    }

    /**
     * Tells if a string is a valid room alias.
     *
     * @param str the string to test
     * @return true if the string is a valid room alias.
     */
    fun isRoomAlias(str: String?): Boolean {
        return str != null &&
            str.length <= MAX_IDENTIFIER_LENGTH &&
            str matches PATTERN_CONTAIN_MATRIX_ALIAS
    }

    /**
     * Tells if a string is a valid event id.
     *
     * @param str the string to test
     * @return true if the string is a valid event id.
     */
    fun isEventId(str: String?): Boolean {
        return str != null &&
            str.length <= MAX_IDENTIFIER_LENGTH &&
            (str matches PATTERN_CONTAIN_MATRIX_EVENT_IDENTIFIER_V4 ||
                str matches PATTERN_CONTAIN_MATRIX_EVENT_IDENTIFIER_V3 ||
                str matches PATTERN_CONTAIN_MATRIX_EVENT_IDENTIFIER)
    }

    /**
     * Tells if a string is a valid thread id. This is an alias for [isEventId].
     *
     * @param str the string to test
     * @return true if the string is a valid thread id.
     */
    fun isThreadId(str: String?) = isEventId(str)

    /**
     * Finds existing ids or aliases in a [CharSequence].
     * Note not all cases are implemented.
     */
    fun findPatterns(text: CharSequence, permalinkParser: PermalinkParser): List<MatrixPatternResult> {
        val rawTextMatches = "\\S+$DOMAIN_REGEX".toRegex(RegexOption.IGNORE_CASE).findAll(text)
        val urlMatches = "\\[\\S+\\]\\((\\S+)\\)".toRegex(RegexOption.IGNORE_CASE).findAll(text)
        val atRoomMatches = Regex("@room").findAll(text)
        return buildList {
            for (match in rawTextMatches) {
                // Match existing id and alias patterns in the text
                val type = when {
                    isUserId(match.value) -> MatrixPatternType.USER_ID
                    isRoomId(match.value) -> MatrixPatternType.ROOM_ID
                    isRoomAlias(match.value) -> MatrixPatternType.ROOM_ALIAS
                    isEventId(match.value) -> MatrixPatternType.EVENT_ID
                    else -> null
                }
                if (type != null) {
                    add(MatrixPatternResult(type, match.value, match.range.first, match.range.last + 1))
                }
            }
            for (match in urlMatches) {
                // Extract the link and check if it's a valid permalink
                val urlMatch = match.groupValues[1]
                when (val permalink = permalinkParser.parse(urlMatch)) {
                    is PermalinkData.UserLink -> {
                        add(MatrixPatternResult(MatrixPatternType.USER_ID, permalink.userId.value, match.range.first, match.range.last + 1))
                    }
                    is PermalinkData.RoomLink -> {
                        when (permalink.roomIdOrAlias) {
                            is RoomIdOrAlias.Alias -> MatrixPatternType.ROOM_ALIAS
                            is RoomIdOrAlias.Id -> if (permalink.eventId == null) MatrixPatternType.ROOM_ID else null
                        }?.let { type ->
                            add(MatrixPatternResult(type, permalink.roomIdOrAlias.identifier, match.range.first, match.range.last + 1))
                        }
                    }
                    else -> Unit
                }
            }
            for (match in atRoomMatches) {
                // Special case for `@room` mentions
                add(MatrixPatternResult(MatrixPatternType.AT_ROOM, match.value, match.range.first, match.range.last + 1))
            }
        }
    }
}

enum class MatrixPatternType {
    USER_ID,
    ROOM_ID,
    ROOM_ALIAS,
    EVENT_ID,
    AT_ROOM
}

data class MatrixPatternResult(val type: MatrixPatternType, val value: String, val start: Int, val end: Int)
