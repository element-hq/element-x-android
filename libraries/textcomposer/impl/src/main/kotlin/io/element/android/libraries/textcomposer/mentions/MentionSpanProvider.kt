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

package io.element.android.libraries.textcomposer.mentions

import androidx.compose.runtime.Stable
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import javax.inject.Inject

@Stable
class MentionSpanProvider @Inject constructor(
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
                    rawValue = permalinkData.roomIdOrAlias.toString(),
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
