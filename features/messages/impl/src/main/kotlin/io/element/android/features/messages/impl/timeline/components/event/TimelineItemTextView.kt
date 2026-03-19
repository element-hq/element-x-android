/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import android.text.Layout
import android.text.SpannedString
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayout
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContentProvider
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.features.messages.impl.urlpreview.LocalUrlPreviewService
import io.element.android.features.messages.impl.urlpreview.UrlPreviewData
import io.element.android.features.messages.impl.urlpreview.findFirstPreviewableUrl
import io.element.android.features.messages.impl.utils.containsOnlyEmojis
import io.element.android.libraries.androidutils.text.LinkifyHelper
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.roundToPx
import io.element.android.libraries.textcomposer.ElementRichTextEditorStyle
import io.element.android.libraries.textcomposer.mentions.LocalMentionSpanUpdater
import io.element.android.wysiwyg.compose.EditorStyledText
import io.element.android.wysiwyg.link.Link

@Composable
fun TimelineItemTextView(
    content: TimelineItemTextBasedContent,
    showUrlPreviews: Boolean,
    onLinkClick: (Link) -> Unit,
    onLinkLongClick: (Link) -> Unit,
    modifier: Modifier = Modifier,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit = {},
) {
    val emojiOnly = content.formattedBody.toString() == content.body &&
        content.body.replace(" ", "").containsOnlyEmojis()
    val textStyle = when {
        emojiOnly -> ElementTheme.typography.fontHeadingXlRegular
        else -> ElementTheme.typography.fontBodyLgRegular
    }
    CompositionLocalProvider(
        LocalContentColor provides ElementTheme.colors.textPrimary,
        LocalTextStyle provides textStyle
    ) {
        val text = getTextWithResolvedMentions(content)
        val urlPreviewService = LocalUrlPreviewService.current
        val previewUrl = remember(content.formattedBody, content.htmlDocument) {
            findFirstPreviewableUrl(content.formattedBody, content.htmlDocument)
        }
        val urlPreview by produceState<UrlPreviewData?>(null, previewUrl, showUrlPreviews, urlPreviewService) {
            value = if (showUrlPreviews && previewUrl != null) {
                urlPreviewService.getPreview(previewUrl).getOrNull()
            } else {
                null
            }
        }
        if (urlPreview != null) {
            val density = LocalDensity.current
            var previewSize by remember { mutableStateOf(Size.Zero) }
            var textLayout by remember { mutableStateOf<Layout?>(null) }
            val spacingPx = with(density) { 6.dp.roundToPx() }
            val minPreviewWidthPx = with(density) { 244.dp.roundToPx() }
            val maxPreviewWidthPx = with(density) { 296.dp.roundToPx() }
            var previewCardWidthPx by remember(minPreviewWidthPx) { mutableIntStateOf(minPreviewWidthPx) }

            fun updateLayoutData() {
                val currentTextLayout = textLayout ?: return
                previewCardWidthPx = currentTextLayout.width.coerceIn(minPreviewWidthPx, maxPreviewWidthPx)
                val previewWidth = previewSize.width.toInt()
                val previewHeight = previewSize.height.toInt()
                val textWidth = currentTextLayout.width
                val textHeight = currentTextLayout.height
                val lastLineWidth = currentTextLayout.getLineWidth(currentTextLayout.lineCount - 1).toInt()
                val lastLineHeight = currentTextLayout.getLineBottom(currentTextLayout.lineCount - 1)
                onContentLayoutChange(
                    ContentAvoidingLayoutData(
                        contentWidth = maxOf(previewWidth, textWidth),
                        contentHeight = previewHeight + spacingPx + textHeight,
                        nonOverlappingContentWidth = lastLineWidth,
                        nonOverlappingContentHeight = previewHeight + spacingPx + lastLineHeight,
                    )
                )
            }

            Column(
                modifier = modifier
                    .semantics { contentDescription = content.plainText }
            ) {
                TimelineItemUrlPreviewView(
                    preview = requireNotNull(urlPreview),
                    onClick = onLinkClick,
                    onLongClick = onLinkLongClick,
                    cardWidth = with(density) { previewCardWidthPx.toDp() },
                    modifier = Modifier.onSizeChanged { size ->
                        previewSize = Size(size.width.toFloat(), size.height.toFloat())
                        updateLayoutData()
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
                EditorStyledText(
                    text = text,
                    onLinkClickedListener = onLinkClick,
                    onLinkLongClickedListener = onLinkLongClick,
                    style = ElementRichTextEditorStyle.textStyle(),
                    onTextLayout = { layout ->
                        textLayout = layout
                        updateLayoutData()
                    },
                    releaseOnDetach = false,
                )
            }
        } else {
            Box(modifier.semantics { contentDescription = content.plainText }) {
                EditorStyledText(
                    text = text,
                    onLinkClickedListener = onLinkClick,
                    onLinkLongClickedListener = onLinkLongClick,
                    style = ElementRichTextEditorStyle.textStyle(),
                    onTextLayout = ContentAvoidingLayout.measureLegacyLastTextLine(onContentLayoutChange = onContentLayoutChange),
                    releaseOnDetach = false,
                )
            }
        }
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun getTextWithResolvedMentions(content: TimelineItemTextBasedContent): CharSequence {
    val mentionSpanUpdater = LocalMentionSpanUpdater.current
    val bodyWithResolvedMentions = mentionSpanUpdater.rememberMentionSpans(content.formattedBody)
    return SpannedString.valueOf(bodyWithResolvedMentions)
}

@PreviewsDayNight
@Composable
internal fun TimelineItemTextViewPreview(
    @PreviewParameter(TimelineItemTextBasedContentProvider::class) content: TimelineItemTextBasedContent
) = ElementPreview {
    TimelineItemTextView(
        content = content,
        showUrlPreviews = false,
        onLinkClick = {},
        onLinkLongClick = {},
    )
}

@Preview
@Composable
internal fun TimelineItemTextViewWithLinkifiedUrlPreview() = ElementPreview {
    val content = aTimelineItemTextContent(
        formattedBody = LinkifyHelper.linkify("The link should end after the first '?' (url: github.com/element-hq/element-x-android/README?)?.")
    )
    TimelineItemTextView(
        content = content,
        showUrlPreviews = false,
        onLinkClick = {},
        onLinkLongClick = {},
    )
}

@Preview
@Composable
internal fun TimelineItemTextViewWithLinkifiedUrlAndNestedParenthesisPreview() = ElementPreview {
    val content = aTimelineItemTextContent(
        formattedBody = LinkifyHelper.linkify("The link should end after the '(ME)' ((url: github.com/element-hq/element-x-android/READ(ME)))!")
    )
    TimelineItemTextView(
        content = content,
        showUrlPreviews = false,
        onLinkClick = {},
        onLinkLongClick = {},
    )
}
