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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
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
    fun setup() {
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
