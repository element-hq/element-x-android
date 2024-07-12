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

// @PreviewsDayNight
// @Composable
// internal fun MentionSpanPreview() {
//    ElementPreview {
//        val provider = remember {
//            MentionSpanProvider(
//                currentSessionId = "@me:matrix.org",
//                permalinkParser = object : PermalinkParser {
//                    override fun parse(uriString: String): PermalinkData {
//                        return when (uriString) {
//                            "https://matrix.to/#/@me:matrix.org" -> PermalinkData.UserLink(UserId("@me:matrix.org"))
//                            "https://matrix.to/#/@other:matrix.org" -> PermalinkData.UserLink(UserId("@other:matrix.org"))
//                            "https://matrix.to/#/#room:matrix.org" -> PermalinkData.RoomLink(
//                                roomIdOrAlias = RoomAlias("#room:matrix.org").toRoomIdOrAlias(),
//                                eventId = null,
//                                viaParameters = persistentListOf(),
//                            )
//                            else -> throw AssertionError("Unexpected value $uriString")
//                        }
//                    }
//                },
//            )
//        }
//        provider.updateStyles()
//
//        val textColor = ElementTheme.colors.textPrimary.toArgb()
//        fun mentionSpanMe() = provider.getMentionSpanFor("mention", "https://matrix.to/#/@me:matrix.org")
//        fun mentionSpanOther() = provider.getMentionSpanFor("mention", "https://matrix.to/#/@other:matrix.org")
//        fun mentionSpanRoom() = provider.getMentionSpanFor("room", "https://matrix.to/#/#room:matrix.org")
//        AndroidView(factory = { context ->
//            TextView(context).apply {
//                includeFontPadding = false
//                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//                text = buildSpannedString {
//                    append("This is a ")
//                    append("@mention", mentionSpanMe(), 0)
//                    append(" to the current user and this is a ")
//                    append("@mention", mentionSpanOther(), 0)
//                    append(" to other user. This one is for a room: ")
//                    append("#room:matrix.org", mentionSpanRoom(), 0)
//                    append("\n\n")
//                    append("This ")
//                    append("mention", mentionSpanMe(), 0)
//                    append(" didn't have an '@' and it was automatically added, same as this ")
//                    append("room:matrix.org", mentionSpanRoom(), 0)
//                    append(" one, which had no leading '#'.")
//                }
//                setTextColor(textColor)
//            }
//        })
//    }
// }

