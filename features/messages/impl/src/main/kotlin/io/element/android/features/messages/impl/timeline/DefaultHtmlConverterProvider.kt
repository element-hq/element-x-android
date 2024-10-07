/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.messages.api.timeline.HtmlConverterProvider
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.textcomposer.ElementRichTextEditorStyle
import io.element.android.libraries.textcomposer.mentions.LocalMentionSpanTheme
import io.element.android.libraries.textcomposer.mentions.MentionSpanProvider
import io.element.android.wysiwyg.compose.StyledHtmlConverter
import io.element.android.wysiwyg.display.MentionDisplayHandler
import io.element.android.wysiwyg.display.TextDisplay
import io.element.android.wysiwyg.utils.HtmlConverter
import uniffi.wysiwyg_composer.newMentionDetector
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
@SingleIn(SessionScope::class)
class DefaultHtmlConverterProvider @Inject constructor(
    private val mentionSpanProvider: MentionSpanProvider,
) : HtmlConverterProvider {
    private val htmlConverter: MutableState<HtmlConverter?> = mutableStateOf(null)

    @Composable
    override fun Update(currentUserId: UserId) {
        val isInEditMode = LocalInspectionMode.current
        val mentionDetector = remember(isInEditMode) {
            if (isInEditMode) null else newMentionDetector()
        }

        val editorStyle = ElementRichTextEditorStyle.textStyle()
        val mentionSpanTheme = LocalMentionSpanTheme.current
        val context = LocalContext.current

        htmlConverter.value = remember(editorStyle, mentionSpanTheme) {
            StyledHtmlConverter(
                context = context,
                mentionDisplayHandler = object : MentionDisplayHandler {
                    override fun resolveAtRoomMentionDisplay(): TextDisplay {
                        val mentionSpan = mentionSpanProvider.getMentionSpanFor(text = "@room", url = "#")
                        mentionSpan.update(mentionSpanTheme)
                        return TextDisplay.Custom(mentionSpan)
                    }

                    override fun resolveMentionDisplay(text: String, url: String): TextDisplay {
                        val mentionSpan = mentionSpanProvider.getMentionSpanFor(text, url)
                        mentionSpan.update(mentionSpanTheme)
                        return TextDisplay.Custom(mentionSpan)
                    }
                },
                isMention = { _, url -> mentionDetector?.isMention(url).orFalse() }
            ).apply {
                configureWith(editorStyle)
            }
        }
    }

    override fun provide(): HtmlConverter {
        return htmlConverter.value ?: error("HtmlConverter wasn't instantiated. Make sure to call HtmlConverterProvider.Update() first.")
    }
}
