/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import android.text.SpannedString
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayout
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.model.event.AN_EMOJI_ONLY_TEXT
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContentPreviewParam
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.features.messages.impl.utils.containsOnlyEmojis
import io.element.android.libraries.androidutils.text.LinkifyHelper
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.utils.LocalUiTestMode
import io.element.android.libraries.textcomposer.ElementRichTextEditorStyle
import io.element.android.libraries.textcomposer.mentions.LocalMentionSpanUpdater
import io.element.android.wysiwyg.compose.EditorStyledText
import io.element.android.wysiwyg.link.Link
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun TimelineItemTextView(
    content: TimelineItemTextBasedContent,
    onLinkClick: (Link) -> Unit,
    onLinkLongClick: (Link) -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit = {},
) {
    // The View <-> Compose interop is not working well with Compose UI tests (it loops indefinitely), so we skip it in the UI test mode.
    if (LocalUiTestMode.current) return

    val isInPreview = LocalInspectionMode.current
    val emojiOnly = remember(content.body, content.formattedBody, isInPreview) {
        content.formattedBody.toString() == content.body &&
            content.body.replace(" ", "").let { body ->
                if (isInPreview) body == AN_EMOJI_ONLY_TEXT else body.containsOnlyEmojis()
            }
    }
    val textStyle = when {
        emojiOnly -> ElementTheme.typography.fontHeadingXlRegular
        else -> ElementTheme.typography.fontBodyLgRegular
    }
    CompositionLocalProvider(
        LocalContentColor provides ElementTheme.colors.textPrimary,
        LocalTextStyle provides textStyle
    ) {
        val text = getTextWithResolvedMentions(content)
        val codeBlockBounds = remember { mutableStateOf<ImmutableList<CodeBlockBounds>>(persistentListOf()) }
        val measureLastTextLine = ContentAvoidingLayout.measureLegacyLastTextLine(onContentLayoutChange = onContentLayoutChange)
        val density = LocalDensity.current
        val headerPx = with(density) { (CodeBlockHeaderHeight * fontScale).roundToPx() }
        val footerPx = with(density) { (CodeBlockFooterHeight * fontScale).roundToPx() }
        val languages = remember(content.htmlDocument) { codeBlockLanguages(content.htmlDocument) }
        // Not remembered: a stable identity would stop EditorStyledText from calling setText again,
        // and in-place MentionSpan updates would never reach the TextView.
        val displayText = withCodeBlockChrome(text, headerPx, footerPx, languages)
        val actions = codeBlockActions(displayText, languages)
        Box(modifier.semantics { contentDescription = content.plainText }) {
            EditorStyledText(
                text = displayText,
                onLinkClickedListener = onLinkClick,
                onLinkLongClickedListener = onLinkLongClick,
                style = ElementRichTextEditorStyle.textStyle(),
                onTextLayout = { layout ->
                    measureLastTextLine(layout)
                    codeBlockBounds.value = computeCodeBlockBounds(displayText, layout)
                },
                releaseOnDetach = false,
            )
            CodeBlockCopyButtons(
                actions = actions,
                boundsAt = { index -> codeBlockBounds.value.getOrNull(index) ?: CodeBlockBounds.Zero },
                onLongClick = onLongClick,
            )
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
    @PreviewParameter(TimelineItemTextBasedContentPreviewParam::class) content: TimelineItemTextBasedContent
) = ElementPreview {
    TimelineItemTextView(
        content = content,
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
        onLinkClick = {},
        onLinkLongClick = {},
    )
}
