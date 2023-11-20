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
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.buildSpannedString
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.currentUserMentionPillBackground
import io.element.android.libraries.designsystem.theme.currentUserMentionPillText
import io.element.android.libraries.designsystem.theme.mentionPillBackground
import io.element.android.libraries.designsystem.theme.mentionPillText
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.theme.ElementTheme

@Stable
class MentionSpanProvider(
    private val currentSessionId: SessionId,
    private var currentUserTextColor: Int = 0,
    private var currentUserBackgroundColor: Int = Color.WHITE,
    private var otherTextColor: Int = 0,
    private var otherBackgroundColor: Int = Color.WHITE,
) {

    @Suppress("ComposableNaming")
    @Composable
    internal fun setup() {
        currentUserTextColor = ElementTheme.colors.currentUserMentionPillText.toArgb()
        currentUserBackgroundColor = ElementTheme.colors.currentUserMentionPillBackground.toArgb()
        otherTextColor = ElementTheme.colors.mentionPillText.toArgb()
        otherBackgroundColor = ElementTheme.colors.mentionPillBackground.toArgb()
    }

    fun getMentionSpanFor(text: String, url: String): MentionSpan {
        val permalinkData = PermalinkParser.parse(url)
        return when {
            permalinkData is PermalinkData.UserLink -> {
                val isCurrentUser = permalinkData.userId == currentSessionId.value
                MentionSpan(
                    backgroundColor = if (isCurrentUser) currentUserBackgroundColor else otherBackgroundColor,
                    textColor = if (isCurrentUser) currentUserTextColor else otherTextColor,
                )
            }
            text == "@room" && permalinkData is PermalinkData.FallbackLink -> {
                MentionSpan(
                    backgroundColor = otherBackgroundColor,
                    textColor = otherTextColor,
                )
            }
            else -> {
                MentionSpan(
                    backgroundColor = otherBackgroundColor,
                    textColor = otherTextColor,
                )
            }
        }
    }
}

@Composable
fun rememberMentionSpanProvider(currentUserId: SessionId): MentionSpanProvider {
    val provider = remember(currentUserId) {
        MentionSpanProvider(currentUserId)
    }
    provider.setup()
    return provider
}

@PreviewsDayNight
@Composable
internal fun MentionSpanPreview() {
    val provider = rememberMentionSpanProvider(SessionId("@me:matrix.org"))
    ElementPreview {
        provider.setup()

        val textColor = ElementTheme.colors.textPrimary.toArgb()
        val mentionSpan = provider.getMentionSpanFor("me", "https://matrix.to/#/@me:matrix.org")
        val mentionSpan2 = provider.getMentionSpanFor("other", "https://matrix.to/#/@other:matrix.org")
        AndroidView(factory = { context ->
            TextView(context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                text = buildSpannedString {
                    append("This is a ")
                    append("@mention", mentionSpan, 0)
                    append(" to the current user and this is a ")
                    append("@mention", mentionSpan2, 0)
                    append(" to other user")
                }
                setTextColor(textColor)
            }
        })
    }
}
