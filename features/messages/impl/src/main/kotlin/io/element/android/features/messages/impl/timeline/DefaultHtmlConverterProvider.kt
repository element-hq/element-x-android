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
import io.element.android.libraries.textcomposer.mentions.rememberMentionSpanProvider
import io.element.android.wysiwyg.compose.StyledHtmlConverter
import io.element.android.wysiwyg.display.MentionDisplayHandler
import io.element.android.wysiwyg.display.TextDisplay
import io.element.android.wysiwyg.utils.HtmlConverter
import uniffi.wysiwyg_composer.newMentionDetector
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
@SingleIn(SessionScope::class)
class DefaultHtmlConverterProvider @Inject constructor() : HtmlConverterProvider {

    private val htmlConverter: MutableState<HtmlConverter?> = mutableStateOf(null)

    @Composable
    override fun Update(currentUserId: UserId) {
        val isInEditMode = LocalInspectionMode.current
        val mentionDetector = remember(isInEditMode) {
            if (isInEditMode) { null } else { newMentionDetector() }
        }

        val editorStyle = ElementRichTextEditorStyle.textStyle()
        val mentionSpanProvider = rememberMentionSpanProvider(currentUserId = currentUserId)

        val context = LocalContext.current

        htmlConverter.value = remember(editorStyle, mentionSpanProvider) {
            StyledHtmlConverter(
                context = context,
                mentionDisplayHandler = object : MentionDisplayHandler {
                    override fun resolveAtRoomMentionDisplay(): TextDisplay {
                        return TextDisplay.Custom(mentionSpanProvider.getMentionSpanFor(text = "@room", url = "#"))
                    }

                    override fun resolveMentionDisplay(text: String, url: String): TextDisplay {
                        return TextDisplay.Custom(mentionSpanProvider.getMentionSpanFor(text, url))
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
