/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.api.ui

import android.content.ClipData
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.components.linkify
import io.element.android.libraries.designsystem.text.toPx
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.htmlrenderer.api.BlockNode
import io.element.android.libraries.htmlrenderer.api.CodeBlockNode
import io.element.android.libraries.htmlrenderer.api.DocumentNode
import io.element.android.libraries.htmlrenderer.api.HeaderNode
import io.element.android.libraries.htmlrenderer.api.HtmlMessageParser.Companion.INLINE_CODE_ANNOTATION_TAG
import io.element.android.libraries.htmlrenderer.api.HtmlMessageParser.Companion.LINK_ANNOTATION_TAG
import io.element.android.libraries.htmlrenderer.api.ListNode
import io.element.android.libraries.htmlrenderer.api.MentionNodeContent
import io.element.android.libraries.htmlrenderer.api.ParagraphNode
import io.element.android.libraries.htmlrenderer.api.QuoteNode
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.ui.common.layout.ContentAvoidingLayout
import io.element.android.libraries.ui.common.layout.ContentAvoidingLayoutData
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.launch

/**
 * Renders a parsed message tree ([DocumentNode]) as native Compose content.
 *
 * The renderer is stateless: it is driven entirely by [node] and reports interactions
 * through the callbacks. It performs no HTML parsing itself.
 *
 * @param node the root HTML node of the parsed message tree to render.
 * @param modifier the [Modifier] to be applied to the layout.
 * @param currentUserId the current user ID, if known. This can be used to display UI sent by/owned by the current user differently.
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
    BlockNodes(nodes = node.children, context = context, modifier = modifier)
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
    is HeaderNode -> this
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
    if (node === context.lastBlockNode && node !is ParagraphNode && node !is HeaderNode) {
        SideEffect { context.onContentLayoutChange(ContentAvoidingLayoutData.NotOverlapping) }
    }

    when (node) {
        is DocumentNode -> BlockNodes(nodes = node.children, context = context, modifier = modifier)
        is ParagraphNode -> {
            ParagraphView(node = node, context = context, modifier = modifier)
        }
        is HeaderNode -> HeaderView(node = node, context = context, modifier = modifier)
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
    // Only the last rendered paragraph (and only when not inside a code block / quote) reports its
    // measured last line to the timeline.
    val measureLastLine = if (node === context.lastBlockNode) {
        ContentAvoidingLayout.measureLastTextLine(onContentLayoutChange = context.onContentLayoutChange)
    } else {
        null
    }
    TextBlock(
        text = node.text,
        inlineContent = node.inlineContent,
        textStyle = ElementTheme.typography.fontBodyMdRegular,
        context = context,
        modifier = modifier,
        onTextLayout = { measureLastLine?.invoke(it) },
    )
}

@Composable
private fun HeaderView(
    node: HeaderNode,
    context: RenderContext,
    modifier: Modifier = Modifier,
) {
    // Only the last rendered paragraph (and only when not inside a code block / quote) reports its
    // measured last line to the timeline.
    val measureLastLine = if (node === context.lastBlockNode) {
        ContentAvoidingLayout.measureLastTextLine(onContentLayoutChange = context.onContentLayoutChange)
    } else {
        null
    }
    TextBlock(
        text = node.text,
        inlineContent = node.inlineContent,
        textStyle = headerTextStyle(node.level),
        context = context,
        modifier = modifier,
        onTextLayout = { measureLastLine?.invoke(it) }
    )
}

/**
 * Renders an inline-content block (a paragraph or a header) as a [Text] with [textStyle], applying
 * link styling, the inline-code background boxes, mention/image inline content and link taps.
 */
@Composable
private fun TextBlock(
    text: AnnotatedString,
    inlineContent: ImmutableMap<String, MentionNodeContent>,
    textStyle: TextStyle,
    context: RenderContext,
    modifier: Modifier = Modifier,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    val linkColor = ElementTheme.colors.textLinkExternal
    val codeBackgroundColor = ElementTheme.colors.bgSubtleSecondary
    val codeBorderColor = ElementTheme.colors.borderInteractiveSecondary
    val styledText = remember(text, linkColor) {
        text.linkify(SpanStyle(color = linkColor))
            .applyLinkStyles(linkColor = linkColor)
    }
    val resolvedInlineContent = rememberMentionInlineContent(inlineContent, context)
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    Text(
        text = styledText,
        modifier = modifier
            .drawInlineCodeBackgrounds(styledText, layoutResult, codeBackgroundColor, codeBorderColor, LocalDensity.current)
            .linkTapHandler(styledText, layoutResult, context),
        style = textStyle,
        color = ElementTheme.colors.textPrimary,
        inlineContent = resolvedInlineContent,
        onTextLayout = { result ->
            layoutResult.value = result
            onTextLayout(result)
        },
    )
}

/** Maps an `<h1>`–`<h6>` [level] to a Compound typography style, largest first. */
@Composable
private fun headerTextStyle(level: Int): TextStyle = when (level) {
    // For safety reasons, use the same style for H1 and H2, to avoid people using H1 for 'shouting' spam that fills the timeline with huge text
    1, 2 -> ElementTheme.typography.fontHeadingLgBold
    3 -> ElementTheme.typography.fontHeadingMdBold
    4 -> ElementTheme.typography.fontHeadingSmMedium
    5 -> ElementTheme.typography.fontBodyLgMedium
    else -> ElementTheme.typography.fontBodyMdMedium
}

@Composable
private fun CodeBlockView(
    node: CodeBlockNode,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val localClipboard = LocalClipboard.current
    val clipDataLabel = stringResource(CommonStrings.common_code_block)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = ElementTheme.colors.borderInteractiveSecondary, shape = RoundedCornerShape(8.dp))
            .background(ElementTheme.colors.bgSubtleSecondary)
            .height(IntrinsicSize.Min)
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    coroutineScope.launch {
                        localClipboard.setClipEntry(ClipEntry(ClipData.newPlainText(clipDataLabel, node.code)))
                    }
                },
                onLongClickLabel = stringResource(CommonStrings.action_copy_to_clipboard),
            ),
    ) {
        Text(
            modifier = Modifier.horizontalScroll(scrollState).padding(horizontal = 12.dp, vertical = 8.dp),
            text = node.code,
            style = ElementTheme.typography.fontBodySmRegular.copy(fontFamily = FontFamily.Monospace),
            color = ElementTheme.colors.textPrimary,
            softWrap = false,
        )

        // Previews and screenshots can't render the fading edge gradients properly, everything is obscured by them, so we skip them in the preview mode.
        if (LocalInspectionMode.current.not() && (scrollState.canScrollForward || scrollState.canScrollBackward)) {
            val progress by remember {
                derivedStateOf {
                    scrollState.value.toFloat() / scrollState.maxValue
                }
            }

            val alpha by animateFloatAsState(targetValue = progress, label = "CodeBlockViewScrollAlpha")

            // Draw a fading edge gradient at the left and right of the code block, to hint that it is horizontally scrollable.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(ElementTheme.colors.bgSubtleTertiary.copy(alpha = alpha), Color.Transparent),
                            startX = 0f,
                            endX = CodeBlockFadingEdgeWidth.toPx(),
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, ElementTheme.colors.bgSubtleTertiary.copy(alpha = 1f - alpha)),
                            startX = scrollState.viewportSize - CodeBlockFadingEdgeWidth.toPx(),
                            endX = scrollState.viewportSize.toFloat(),
                        )
                    )
            )
        }
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
                    textAlign = if (node.ordered) TextAlign.Start else TextAlign.Center,
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
        linkRanges.forEach { linkRange ->
            val containsColorSpan =
                spanStyles.any { colorRange -> colorRange.start == linkRange.start && colorRange.end == linkRange.end && colorRange.item.color == linkColor }
            if (!containsColorSpan) {
                addStyle(SpanStyle(color = linkColor), linkRange.start, linkRange.end)
            }
        }
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
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = true)
        val (url, urlText) = layoutResult.value?.urlAt(down.position, text) ?: return@awaitEachGesture
        down.consume()

        val longPressTimeout = viewConfiguration.longPressTimeoutMillis

        val isLongPress = try {
            withTimeout(longPressTimeout) {
                val up = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: return@withTimeout null
                if (up.pressed) return@withTimeout null
                up.consume()

                up.uptimeMillis - down.uptimeMillis > longPressTimeout
            }
        } catch (_: PointerEventTimeoutCancellationException) {
            true
        } ?: return@awaitEachGesture

        if (isLongPress) {
            context.onLinkLongClick(url, urlText)
        } else {
            context.onLinkClick(url, urlText)
        }
    }
}

private fun TextLayoutResult.urlAt(offset: Offset, text: AnnotatedString): Pair<String, String>? {
    val position = getOffsetForPosition(offset)
    val urlAnnotation = text.getStringAnnotations(LINK_ANNOTATION_TAG, position, position).firstOrNull() ?: return null
    return urlAnnotation.item to text.substring(urlAnnotation.start, urlAnnotation.end)
}

private val BlockSpacing: Dp = 8.dp
private val ListItemSpacing: Dp = 4.dp
private val ListMarkerWidth: Dp = 24.dp
private val PillPaddingHorizontal: Dp = 6.dp
private val PillPaddingVertical: Dp = 0.dp

/** Rounded, bordered box drawn behind inline code. */
private val InlineCodeCornerRadius: Dp = 6.dp
private val InlineCodeBorderWidth: Dp = 1.dp
private val InlineCodeHorizontalPadding: Dp = 2.dp

/** The width of the fading edge gradient drawn at the left and right of a [CodeBlockView]. */
private val CodeBlockFadingEdgeWidth: Dp = 32.dp
