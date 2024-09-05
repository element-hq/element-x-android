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

package io.element.android.features.messages.impl.utils

import com.sigpwned.emoji4j.core.Grapheme.Type.EMOJI
import com.sigpwned.emoji4j.core.Grapheme.Type.PICTOGRAPHIC
import com.sigpwned.emoji4j.core.GraphemeMatchResult
import com.sigpwned.emoji4j.core.GraphemeMatcher

/**
 * Returns true if the string consists exclusively of "emoji or pictographic graphemes".
 */
fun String.containsOnlyEmojis(): Boolean {
    if (isEmpty()) return false

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
