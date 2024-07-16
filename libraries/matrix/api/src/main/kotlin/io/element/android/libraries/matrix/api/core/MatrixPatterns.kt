/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

    // See https://spec.matrix.org/v1.11/appendices/#opaque-identifiers
    private const val OPAQUE_ID_REGEX = "[0-9A-Za-z-\\._~]+"

    // regex pattern to find matrix user ids in a string.
    // See https://matrix.org/docs/spec/appendices#historical-user-ids
    // Sadly, we need to relax the regex pattern a bit as there already exist some ids that don't match the spec.
    private const val MATRIX_USER_IDENTIFIER_REGEX = "^@\\S+?$DOMAIN_REGEX$"
    private val PATTERN_CONTAIN_MATRIX_USER_IDENTIFIER = MATRIX_USER_IDENTIFIER_REGEX.toRegex(RegexOption.IGNORE_CASE)

    // regex pattern to match room ids.
    private const val MATRIX_ROOM_IDENTIFIER_REGEX = "^!$OPAQUE_ID_REGEX$DOMAIN_REGEX$"
    private val PATTERN_CONTAIN_MATRIX_ROOM_IDENTIFIER = MATRIX_ROOM_IDENTIFIER_REGEX.toRegex(RegexOption.IGNORE_CASE)

    // regex pattern to match room aliases.
    private const val MATRIX_ROOM_ALIAS_REGEX = "^#\\S+$DOMAIN_REGEX$"
    private val PATTERN_CONTAIN_MATRIX_ALIAS = MATRIX_ROOM_ALIAS_REGEX.toRegex(RegexOption.IGNORE_CASE)

    // regex pattern to match event ids.
    // Sadly, we need to relax the regex pattern a bit as there already exist some ids that don't match the spec.
    private const val MATRIX_EVENT_IDENTIFIER_REGEX = "^\\$$OPAQUE_ID_REGEX$DOMAIN_REGEX$"
    private val PATTERN_CONTAIN_MATRIX_EVENT_IDENTIFIER = MATRIX_EVENT_IDENTIFIER_REGEX.toRegex(RegexOption.IGNORE_CASE)

    private const val MATRIX_EVENT_IDENTIFIER_V4_REGEX = "\\$$OPAQUE_ID_REGEX"
    private val PATTERN_CONTAIN_MATRIX_EVENT_IDENTIFIER_V4 = MATRIX_EVENT_IDENTIFIER_V4_REGEX.toRegex(RegexOption.IGNORE_CASE)

    private const val MAX_IDENTIFIER_LENGTH = 255

    /**
     * Tells if a string is a valid user Id.
     *
     * @param str the string to test
     * @return true if the string is a valid user id
     */
    fun isUserId(str: String?): Boolean {
        return str != null && str matches PATTERN_CONTAIN_MATRIX_USER_IDENTIFIER && str.length <= MAX_IDENTIFIER_LENGTH
    }

    /**
     * Tells if a string is a valid space id. This is an alias for [isRoomId]
     *
     * @param str the string to test
     * @return true if the string is a valid space Id
     */
    fun isSpaceId(str: String?) = isRoomId(str)

    /**
     * Tells if a string is a valid room id.
     *
     * @param str the string to test
     * @return true if the string is a valid room Id
     */
    fun isRoomId(str: String?): Boolean {
        return str != null && str matches PATTERN_CONTAIN_MATRIX_ROOM_IDENTIFIER && str.length <= MAX_IDENTIFIER_LENGTH
    }

    /**
     * Tells if a string is a valid room alias.
     *
     * @param str the string to test
     * @return true if the string is a valid room alias.
     */
    fun isRoomAlias(str: String?): Boolean {
        return str != null && str matches PATTERN_CONTAIN_MATRIX_ALIAS && str.length <= MAX_IDENTIFIER_LENGTH
    }

    /**
     * Tells if a string is a valid event id.
     *
     * @param str the string to test
     * @return true if the string is a valid event id.
     */
    fun isEventId(str: String?): Boolean {
        return str != null &&
            (str matches PATTERN_CONTAIN_MATRIX_EVENT_IDENTIFIER ||
                str matches PATTERN_CONTAIN_MATRIX_EVENT_IDENTIFIER_V4) &&
            str.length <= MAX_IDENTIFIER_LENGTH
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
                        add(MatrixPatternResult(MatrixPatternType.USER_ID, permalink.userId.toString(), match.range.first, match.range.last + 1))
                    }
                    is PermalinkData.RoomLink -> {
                        add(MatrixPatternResult(MatrixPatternType.ROOM_ALIAS, permalink.roomIdOrAlias.identifier, match.range.first, match.range.last + 1))
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
