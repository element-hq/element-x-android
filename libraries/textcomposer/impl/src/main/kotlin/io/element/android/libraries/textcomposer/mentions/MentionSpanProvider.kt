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

import android.graphics.Color
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.buildSpannedString
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.rememberTypeface
import io.element.android.libraries.designsystem.theme.currentUserMentionPillBackground
import io.element.android.libraries.designsystem.theme.currentUserMentionPillText
import io.element.android.libraries.designsystem.theme.mentionPillBackground
import io.element.android.libraries.designsystem.theme.mentionPillText
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import kotlinx.collections.immutable.persistentListOf

@Stable
class MentionSpanProvider(
    private val currentSessionId: SessionId,
    private val permalinkParser: PermalinkParser,
    private var currentUserTextColor: Int = 0,
    private var currentUserBackgroundColor: Int = Color.WHITE,
    private var otherTextColor: Int = 0,
    private var otherBackgroundColor: Int = Color.WHITE,
) {
    private val paddingValues = PaddingValues(start = 4.dp, end = 6.dp)

    private val paddingValuesPx = mutableStateOf(0 to 0)
    private val typeface = mutableStateOf(Typeface.DEFAULT)

    @Suppress("ComposableNaming")
    @Composable
    internal fun setup() {
        currentUserTextColor = ElementTheme.colors.currentUserMentionPillText.toArgb()
        currentUserBackgroundColor = ElementTheme.colors.currentUserMentionPillBackground.toArgb()
        otherTextColor = ElementTheme.colors.mentionPillText.toArgb()
        otherBackgroundColor = ElementTheme.colors.mentionPillBackground.toArgb()

        typeface.value = ElementTheme.typography.fontBodyLgMedium.rememberTypeface().value
        with(LocalDensity.current) {
            val leftPadding = paddingValues.calculateLeftPadding(LocalLayoutDirection.current).roundToPx()
            val rightPadding = paddingValues.calculateRightPadding(LocalLayoutDirection.current).roundToPx()
            paddingValuesPx.value = leftPadding to rightPadding
        }
    }

    fun getMentionSpanFor(text: String, url: String): MentionSpan {
        val permalinkData = permalinkParser.parse(url)
        val (startPaddingPx, endPaddingPx) = paddingValuesPx.value
        return when {
            permalinkData is PermalinkData.UserLink -> {
                val isCurrentUser = permalinkData.userId == currentSessionId
                MentionSpan(
                    text = text,
                    rawValue = permalinkData.userId.toString(),
                    type = MentionSpan.Type.USER,
                    backgroundColor = if (isCurrentUser) currentUserBackgroundColor else otherBackgroundColor,
                    textColor = if (isCurrentUser) currentUserTextColor else otherTextColor,
                    startPadding = startPaddingPx,
                    endPadding = endPaddingPx,
                    typeface = typeface.value,
                )
            }
            text == "@room" && permalinkData is PermalinkData.FallbackLink -> {
                MentionSpan(
                    text = text,
                    rawValue = "@room",
                    type = MentionSpan.Type.USER,
                    backgroundColor = otherBackgroundColor,
                    textColor = otherTextColor,
                    startPadding = startPaddingPx,
                    endPadding = endPaddingPx,
                    typeface = typeface.value,
                )
            }
            permalinkData is PermalinkData.RoomLink -> {
                MentionSpan(
                    text = text,
                    rawValue = permalinkData.roomIdOrAlias.toString(),
                    type = MentionSpan.Type.ROOM,
                    backgroundColor = otherBackgroundColor,
                    textColor = otherTextColor,
                    startPadding = startPaddingPx,
                    endPadding = endPaddingPx,
                    typeface = typeface.value,
                )
            }
            else -> {
                MentionSpan(
                    text = text,
                    rawValue = text,
                    type = MentionSpan.Type.ROOM,
                    backgroundColor = otherBackgroundColor,
                    textColor = otherTextColor,
                    startPadding = startPaddingPx,
                    endPadding = endPaddingPx,
                    typeface = typeface.value,
                )
            }
        }
    }
}

@Composable
fun rememberMentionSpanProvider(
    currentUserId: SessionId,
    permalinkParser: PermalinkParser,
): MentionSpanProvider {
    val provider = remember(currentUserId) {
        MentionSpanProvider(
            currentSessionId = currentUserId,
            permalinkParser = permalinkParser,
        )
    }
    provider.setup()
    return provider
}

@PreviewsDayNight
@Composable
internal fun MentionSpanPreview() {
    val provider = rememberMentionSpanProvider(
        currentUserId = SessionId("@me:matrix.org"),
        permalinkParser = object : PermalinkParser {
            override fun parse(uriString: String): PermalinkData {
                return when (uriString) {
                    "https://matrix.to/#/@me:matrix.org" -> PermalinkData.UserLink(UserId("@me:matrix.org"))
                    "https://matrix.to/#/@other:matrix.org" -> PermalinkData.UserLink(UserId("@other:matrix.org"))
                    "https://matrix.to/#/#room:matrix.org" -> PermalinkData.RoomLink(
                        roomIdOrAlias = RoomAlias("#room:matrix.org").toRoomIdOrAlias(),
                        eventId = null,
                        viaParameters = persistentListOf(),
                    )
                    else -> TODO()
                }
            }
        },
    )
    ElementPreview {
        provider.setup()

        val textColor = ElementTheme.colors.textPrimary.toArgb()
        fun mentionSpanMe() = provider.getMentionSpanFor("mention", "https://matrix.to/#/@me:matrix.org")
        fun mentionSpanOther() = provider.getMentionSpanFor("mention", "https://matrix.to/#/@other:matrix.org")
        fun mentionSpanRoom() = provider.getMentionSpanFor("room", "https://matrix.to/#/#room:matrix.org")
        AndroidView(factory = { context ->
            TextView(context).apply {
                includeFontPadding = false
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                text = buildSpannedString {
                    append("This is a ")
                    append("@mention", mentionSpanMe(), 0)
                    append(" to the current user and this is a ")
                    append("@mention", mentionSpanOther(), 0)
                    append(" to other user. This one is for a room: ")
                    append("#room:matrix.org", mentionSpanRoom(), 0)
                    append("\n\n")
                    append("This ")
                    append("mention", mentionSpanMe(), 0)
                    append(" didn't have an '@' and it was automatically added, same as this ")
                    append("room:matrix.org", mentionSpanRoom(), 0)
                    append(" one, which had no leading '#'.")
                }
                setTextColor(textColor)
            }
        })
    }
}
