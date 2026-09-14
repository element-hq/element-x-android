/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.api.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.htmlrenderer.api.BlockNode
import io.element.android.libraries.htmlrenderer.api.CodeBlockNode
import io.element.android.libraries.htmlrenderer.api.DocumentNode
import io.element.android.libraries.htmlrenderer.api.HtmlMessageParser.Companion.INLINE_CODE_ANNOTATION_TAG
import io.element.android.libraries.htmlrenderer.api.HtmlMessageParser.Companion.LINK_ANNOTATION_TAG
import io.element.android.libraries.htmlrenderer.api.ListNode
import io.element.android.libraries.htmlrenderer.api.MentionNodeContent
import io.element.android.libraries.htmlrenderer.api.ParagraphNode
import io.element.android.libraries.htmlrenderer.api.QuoteNode
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.ui.common.layout.ContentAvoidingLayout
import io.element.android.libraries.ui.common.layout.ContentAvoidingLayoutData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap

/**
 * Renders a parsed message tree ([DocumentNode]) as native Compose content.
 *
 * The renderer is stateless: it is driven entirely by [node] and reports interactions
 * through the callbacks. It performs no HTML parsing itself.
 *
 * @param node the root HTML node of the parsed message tree to render.
 * @param modifier applied to the root of the rendered content.
 * @param currentUserId used to style a mention of the current user differently; may be null.
 * @param onLinkClick invoked with the target URL when a link is tapped.
 * @param onLinkLongClick invoked with the target URL when a link is long-pressed.
 * @param onMentionClick invoked when a mention pill is tapped.
 * @param onContentLayoutChange invoked with the measured layout of the very last rendered
 * [ParagraphNode], so the timeline can lay out the timestamp around it. It is only wired to that
 * last paragraph, and only when it is not nested inside a [CodeBlockNode] or [QuoteNode] — in which
 * case no measurement is reported.
 */
@Composable
fun HtmlMessageContent(
    node: DocumentNode,
    modifier: Modifier = Modifier,
    currentUserId: UserId? = null,
    onLinkClick: (url: String, text: String) -> Unit = { _, _ -> },
    onLinkLongClick: (url: String, text: String) -> Unit = { _, _ -> },
    onMentionClick: (MentionNodeContent) -> Unit = {},
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit = {},
) {
    val measuredParagraph = remember(node) { node.lastBlockNode() }
    val context = remember(
        currentUserId,
        onLinkClick,
        onLinkLongClick,
        onMentionClick,
        measuredParagraph,
        onContentLayoutChange
    ) {
        RenderContext(
            currentUserId = currentUserId,
            onLinkClick = onLinkClick,
            onLinkLongClick = onLinkLongClick,
            onMentionClick = onMentionClick,
            lastBlockNode = measuredParagraph,
            onContentLayoutChange = onContentLayoutChange,
        )
    }
    SelectionContainer {
        BlockNodes(nodes = node.children, context = context, modifier = modifier)
    }
}

/** Interaction callbacks and state threaded through the render tree. */
@Immutable
private data class RenderContext(
    val currentUserId: UserId?,
    val onLinkClick: (url: String, text: String) -> Unit,
    val onLinkLongClick: (url: String, text: String) -> Unit,
    val onMentionClick: (MentionNodeContent) -> Unit,
    // The latest descendant BlockNode of the root DocumentNode
    val lastBlockNode: BlockNode?,
    val onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
)

internal fun BlockNode.lastBlockNode(): BlockNode? = when (this) {
    is ParagraphNode -> this
    is DocumentNode -> children.lastOrNull()?.lastBlockNode()
    is ListNode -> this
    is QuoteNode -> this
    is CodeBlockNode -> this
}

@Composable
private fun BlockNodes(
    nodes: ImmutableList<BlockNode>,
    context: RenderContext,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(BlockSpacing),
    ) {
        nodes.forEach { BlockNodeView(node = it, context = context) }
    }
}

@Composable
private fun BlockNodeView(
    node: BlockNode,
    context: RenderContext,
    modifier: Modifier = Modifier,
) {
    if (node === context.lastBlockNode && node !is ParagraphNode) {
        SideEffect { context.onContentLayoutChange(ContentAvoidingLayoutData.NotOverlapping) }
    }

    when (node) {
        is DocumentNode -> BlockNodes(nodes = node.children, context = context, modifier = modifier)
        is ParagraphNode -> {
            ParagraphView(node = node, context = context, modifier = modifier)
        }
        is CodeBlockNode -> CodeBlockView(node = node, modifier = modifier)
        is QuoteNode -> QuoteView(node = node, context = context, modifier = modifier)
        is ListNode -> ListView(node = node, context = context, modifier = modifier)
    }
}

@Composable
private fun ParagraphView(
    node: ParagraphNode,
    context: RenderContext,
    modifier: Modifier = Modifier,
) {
    val linkColor = ElementTheme.colors.textLinkExternal
    val codeBackgroundColor = ElementTheme.colors.bgSubtleSecondary
    val codeBorderColor = ElementTheme.colors.borderInteractiveSecondary
    val styledText = remember(node.text, linkColor) {
        node.text.applyLinkStyles(linkColor = linkColor)
    }
    val inlineContent = rememberMentionInlineContent(node.inlineContent, context)
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    // Only the last rendered paragraph (and only when not inside a code block / quote) reports its
    // measured last line to the timeline.
    val measureLastLine = if (node === context.lastBlockNode) {
        ContentAvoidingLayout.measureLastTextLine(onContentLayoutChange = context.onContentLayoutChange)
    } else {
        null
    }
    Text(
        text = styledText,
        modifier = modifier
            .drawInlineCodeBackgrounds(styledText, layoutResult, codeBackgroundColor, codeBorderColor, LocalDensity.current)
            .linkTapHandler(styledText, layoutResult, context),
        style = ElementTheme.typography.fontBodyMdRegular,
        color = ElementTheme.colors.textPrimary,
        inlineContent = inlineContent,
        onTextLayout = { result ->
            layoutResult.value = result
            measureLastLine?.invoke(result)
        },
    )
}

@Composable
private fun CodeBlockView(
    node: CodeBlockNode,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = ElementTheme.colors.borderInteractiveSecondary, shape = RoundedCornerShape(8.dp))
            .background(ElementTheme.colors.bgSubtleSecondary)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = node.code,
            style = ElementTheme.typography.fontBodySmRegular.copy(fontFamily = FontFamily.Monospace),
            color = ElementTheme.colors.textPrimary,
            softWrap = false,
        )
    }
}

@Composable
private fun QuoteView(
    node: QuoteNode,
    context: RenderContext,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.height(IntrinsicSize.Min)) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(1.dp))
                .background(ElementTheme.colors.borderInteractiveSecondary),
        )
        Spacer(modifier = Modifier.width(8.dp))
        BlockNodes(nodes = node.children, context = context)
    }
}

@Composable
private fun ListView(
    node: ListNode,
    context: RenderContext,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(ListItemSpacing),
    ) {
        node.items.forEachIndexed { index, item ->
            Row {
                Text(
                    text = if (node.ordered) "${node.startIndex + index}." else "•",
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = ElementTheme.colors.textPrimary,
                    modifier = Modifier.widthIn(min = ListMarkerWidth),
                    textAlign = if (node.ordered) TextAlign.End else TextAlign.Center,
                )
                BlockNodes(nodes = item.children, context = context)
            }
        }
    }
}

@Composable
private fun MentionPill(
    mention: MentionNodeContent,
    isCurrentUser: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (isCurrentUser) ElementTheme.colors.bgBadgeAccent else ElementTheme.colors.bgBadgePrimary
    val textColor = if (isCurrentUser) ElementTheme.colors.textBadgeAccent else ElementTheme.colors.textOnSolidPrimary
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(percent = 50))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = PillPaddingHorizontal, vertical = PillPaddingVertical),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = mention.displayText,
            style = ElementTheme.typography.fontBodyLgMedium,
            color = textColor,
            maxLines = 1,
        )
    }
}

/**
 * Builds an [InlineTextContent] entry for each mention placeholder. Each pill is measured so
 * the placeholder reserves exactly the space the pill needs.
 */
@Composable
private fun rememberMentionInlineContent(
    mentions: ImmutableMap<String, MentionNodeContent>,
    context: RenderContext,
): ImmutableMap<String, InlineTextContent> {
    if (mentions.isEmpty()) return persistentMapOf()
    val textMeasurer = rememberTextMeasurer()
    val pillTextStyle = ElementTheme.typography.fontBodyLgMedium
    val density = LocalDensity.current
    return mentions.mapValues { (_, mention) ->
        val measured = textMeasurer.measure(mention.displayText, pillTextStyle)
        val width = with(density) { measured.size.width.toDp() + PillPaddingHorizontal * 2 }
        val height = with(density) { measured.size.height.toDp() + PillPaddingVertical * 2 }
        InlineTextContent(
            placeholder = Placeholder(
                width = with(density) { width.toSp() },
                height = with(density) { height.toSp() },
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
            ),
        ) {
            MentionPill(
                mention = mention,
                isCurrentUser = mention is MentionNodeContent.User && mention.userId == context.currentUserId,
                onClick = { context.onMentionClick(mention) },
            )
        }
    }.toImmutableMap()
}

/** Overlays the theme-dependent link color onto the link-annotated ranges. */
private fun AnnotatedString.applyLinkStyles(linkColor: Color): AnnotatedString {
    val linkRanges = getStringAnnotations(LINK_ANNOTATION_TAG, 0, length)
    if (linkRanges.isEmpty()) return this
    return buildAnnotatedString {
        append(this@applyLinkStyles)
        linkRanges.forEach { addStyle(SpanStyle(color = linkColor), it.start, it.end) }
    }
}

/**
 * Draws a rounded, bordered box behind each inline code ([INLINE_CODE_ANNOTATION_TAG]) range, so
 * inline code matches the look of [CodeBlockView] but only covers the annotated text. A range that
 * wraps across several lines gets one box per line.
 */
private fun Modifier.drawInlineCodeBackgrounds(
    text: AnnotatedString,
    layoutResult: State<TextLayoutResult?>,
    backgroundColor: Color,
    borderColor: Color,
    density: Density,
): Modifier = drawBehind {
    val layout = layoutResult.value ?: return@drawBehind
    val codeRanges = text.getStringAnnotations(INLINE_CODE_ANNOTATION_TAG, 0, text.length)
    if (codeRanges.isEmpty()) return@drawBehind
    val strokeWidth = InlineCodeBorderWidth.toPx()
    val horizontalPadding = InlineCodeHorizontalPadding.toPx()
    codeRanges.forEach { range ->
        val firstLine = layout.getLineForOffset(range.start)
        val lastLine = layout.getLineForOffset(range.end)
        for (line in firstLine..lastLine) {
            val lineStart = maxOf(range.start, layout.getLineStart(line))
            val lineEnd = minOf(range.end, layout.getLineEnd(line, visibleEnd = true))
            if (lineEnd <= lineStart) continue
            val startX = layout.getHorizontalPosition(lineStart, usePrimaryDirection = true)
            val endX = layout.getHorizontalPosition(lineEnd, usePrimaryDirection = true)
            val left = minOf(startX, endX) - horizontalPadding
            val right = maxOf(startX, endX) + horizontalPadding
            val topLeft = Offset(left, layout.getLineTop(line))
            val size = Size(right - left, layout.getLineBottom(line) - layout.getLineTop(line))

            val shape = RoundedCornerShape(
                topStart = if (line == firstLine) InlineCodeCornerRadius else 0.dp,
                topEnd = if (line == lastLine) InlineCodeCornerRadius else 0.dp,
                bottomStart = if (line == firstLine) InlineCodeCornerRadius else 0.dp,
                bottomEnd = if (line == lastLine) InlineCodeCornerRadius else 0.dp,
            )

            val outline = shape.createOutline(
                size = size,
                layoutDirection = layoutDirection,
                density = density,
            )

            translate(topLeft.x, topLeft.y) {
                drawOutline(outline = outline, color = backgroundColor, style = Fill)
                drawOutline(outline = outline, color = borderColor, style = Stroke(width = strokeWidth))
            }
        }
    }
}

private fun Modifier.linkTapHandler(
    text: AnnotatedString,
    layoutResult: State<TextLayoutResult?>,
    context: RenderContext,
): Modifier = pointerInput(text) {
    detectTapGestures(
        onTap = { offset ->
            val url = layoutResult.value?.urlAt(offset, text) ?: return@detectTapGestures
            context.onLinkClick(url, text.text)
        },
        onLongPress = { offset ->
            val url = layoutResult.value?.urlAt(offset, text) ?: return@detectTapGestures
            context.onLinkLongClick(url, text.text)
        },
    )
}

private fun TextLayoutResult.urlAt(offset: Offset, text: AnnotatedString): String? {
    val position = getOffsetForPosition(offset)
    return text.getStringAnnotations(LINK_ANNOTATION_TAG, position, position).firstOrNull()?.item
}

private val BlockSpacing: Dp = 8.dp
private val ListItemSpacing: Dp = 4.dp
private val ListMarkerWidth: Dp = 24.dp
private val PillPaddingHorizontal: Dp = 6.dp
private val PillPaddingVertical: Dp = 2.dp

/** Rounded, bordered box drawn behind inline code. */
private val InlineCodeCornerRadius: Dp = 6.dp
private val InlineCodeBorderWidth: Dp = 1.dp
private val InlineCodeHorizontalPadding: Dp = 2.dp
