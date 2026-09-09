/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.utils

import com.sigpwned.emoji4j.core.Grapheme.Type.EMOJI
import com.sigpwned.emoji4j.core.Grapheme.Type.PICTOGRAPHIC
import com.sigpwned.emoji4j.core.GraphemeMatchResult
import com.sigpwned.emoji4j.core.GraphemeMatcher

/**
 * Returns true if the string consists exclusively of "emoji or pictographic graphemes".
 */
internal fun String.containsOnlyEmojis(): Boolean {
    val matcher = GraphemeMatcher(this)
    var m: GraphemeMatchResult? = null
    var contiguous = true
    var previous = 0
    while (contiguous && matcher.find()) {
        m = matcher.toMatchResult()
        // Many non-"emoji" characters are pictographics. We only want to identify this specific range
        // https://en.wikipedia.org/wiki/Miscellaneous_Symbols_and_Pictographs
        val isEmoji = m!!.grapheme().type == EMOJI || m.grapheme().type == PICTOGRAPHIC && m.group() in "🌍".."🗺"
        contiguous = isEmoji and (m.start() == previous)
        previous = m.end()
    }

    return contiguous and (m?.end() == length)
}
