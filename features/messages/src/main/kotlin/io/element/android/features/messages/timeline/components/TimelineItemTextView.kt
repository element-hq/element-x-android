/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.features.messages.timeline.components

import android.text.SpannableString
import android.text.style.URLSpan
import android.text.util.Linkify.PHONE_NUMBERS
import android.text.util.Linkify.WEB_URLS
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.core.text.util.LinkifyCompat
import io.element.android.features.messages.timeline.components.html.HtmlDocument
import io.element.android.features.messages.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.features.messages.timeline.model.event.TimelineItemTextBasedContentProvider
import io.element.android.libraries.designsystem.LinkColor
import io.element.android.libraries.designsystem.components.ClickableLinkText
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight

@Composable
fun TimelineItemTextView(
    content: TimelineItemTextBasedContent,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    onTextClicked: () -> Unit = {},
    onTextLongClicked: () -> Unit = {},
) {
    val htmlDocument = content.htmlDocument
    if (htmlDocument != null) {
        HtmlDocument(
            document = htmlDocument,
            modifier = modifier,
            onTextClicked = onTextClicked,
            onTextLongClicked = onTextLongClicked,
            interactionSource = interactionSource
        )
    } else {
        Box(modifier) {
            val linkStyle = SpanStyle(
                color = LinkColor,
            )
            val styledText = remember(content.body) { content.body.linkify(linkStyle) }
            ClickableLinkText(
                text = styledText,
                linkAnnotationTag = "URL",
                onClick = onTextClicked,
                onLongClick = onTextLongClicked,
                interactionSource = interactionSource
            )
        }
    }
}

private fun String.linkify(
    linkStyle: SpanStyle,
) = buildAnnotatedString {
    append(this@linkify)
    val spannable = SpannableString(this@linkify)
    LinkifyCompat.addLinks(spannable, WEB_URLS or PHONE_NUMBERS)

    val spans = spannable.getSpans(0, spannable.length, URLSpan::class.java)
    for (span in spans) {
        val start = spannable.getSpanStart(span)
        val end = spannable.getSpanEnd(span)
        addStyle(
            start = start,
            end = end,
            style = linkStyle,
        )
        addStringAnnotation(
            tag = "URL",
            annotation = span.url,
            start = start,
            end = end
        )
    }
}

@Preview
@Composable
internal fun TimelineItemTextViewLightPreview(@PreviewParameter(TimelineItemTextBasedContentProvider::class) content: TimelineItemTextBasedContent) =
    ElementPreviewLight { ContentToPreview(content) }

@Preview
@Composable
internal fun TimelineItemTextViewDarkPreview(@PreviewParameter(TimelineItemTextBasedContentProvider::class) content: TimelineItemTextBasedContent) =
    ElementPreviewDark { ContentToPreview(content) }

@Composable
fun ContentToPreview(content: TimelineItemTextBasedContent) {
    TimelineItemTextView(content, MutableInteractionSource())
}

