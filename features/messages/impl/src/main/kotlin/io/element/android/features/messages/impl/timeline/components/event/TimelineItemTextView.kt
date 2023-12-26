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

package io.element.android.features.messages.impl.timeline.components.event

import android.text.SpannableString
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContentProvider
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.textcomposer.ElementRichTextEditorStyle
import io.element.android.wysiwyg.compose.EditorStyledText
import kotlin.math.roundToInt

@Composable
fun TimelineItemTextView(
    content: TimelineItemTextBasedContent,
    onLinkClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
    onContentLayoutChanged: (ContentAvoidingLayoutData) -> Unit = {},
) {
    CompositionLocalProvider(
        LocalContentColor provides ElementTheme.colors.textPrimary,
        LocalTextStyle provides ElementTheme.typography.fontBodyLgRegular
    ) {

        val formattedBody = content.formattedBody
        val body = SpannableString(formattedBody ?: content.body)

        Box(modifier) {
            EditorStyledText(
                text = body,
                onLinkClickedListener = onLinkClicked,
                style = ElementRichTextEditorStyle.textStyle(),
                onTextLayout = { textLayout ->
                    val layoutData =
                        ContentAvoidingLayoutData(
                            contentWidth = textLayout.width,
                            nonOverlappingContentWidth = textLayout.getLineWidth(textLayout.lineCount - 1).roundToInt(),
                            contentHeight = textLayout.height,
                            hasPadding = true,
                        )
                    onContentLayoutChanged(layoutData)
                }
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemTextViewPreview(
    @PreviewParameter(TimelineItemTextBasedContentProvider::class) content: TimelineItemTextBasedContent
) = ElementPreview {
    TimelineItemTextView(
        content = content,
        onLinkClicked = {},
    )
}
