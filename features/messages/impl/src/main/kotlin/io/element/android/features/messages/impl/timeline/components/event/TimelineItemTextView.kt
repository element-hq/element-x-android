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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.sp
import io.element.android.features.messages.impl.timeline.components.html.HtmlDocument
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContentProvider
import io.element.android.libraries.designsystem.components.ClickableLinkText
import io.element.android.libraries.composeutils.annotations.PreviewsDayNight
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.text.toAnnotatedString
import io.element.android.libraries.theme.ElementTheme

@Composable
fun TimelineItemTextView(
    content: TimelineItemTextBasedContent,
    interactionSource: MutableInteractionSource,
    extraPadding: ExtraPadding,
    modifier: Modifier = Modifier,
    onTextClicked: () -> Unit = {},
    onTextLongClicked: () -> Unit = {},
) {
    CompositionLocalProvider(LocalContentColor provides ElementTheme.colors.textPrimary) {
        val htmlDocument = content.htmlDocument
        if (htmlDocument != null) {
            HtmlDocument(
                document = htmlDocument,
                extraPadding = extraPadding,
                modifier = modifier,
                onTextClicked = onTextClicked,
                onTextLongClicked = onTextLongClicked,
                interactionSource = interactionSource
            )
        } else {
            Box(modifier) {
                val textWithPadding = remember(content.body) {
                    content.body + extraPadding.getStr(16.sp).toAnnotatedString()
                }
                ClickableLinkText(
                    text = textWithPadding,
                    onClick = onTextClicked,
                    onLongClick = onTextLongClicked,
                    interactionSource = interactionSource
                )
            }
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
        interactionSource = remember { MutableInteractionSource() },
        extraPadding = ExtraPadding(nbChars = 8),
    )
}
