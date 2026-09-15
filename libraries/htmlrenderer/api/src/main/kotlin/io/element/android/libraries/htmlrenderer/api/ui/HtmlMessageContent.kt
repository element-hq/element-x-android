/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.api.ui

import android.content.ClipData
import android.content.ClipData.newPlainText
import android.content.ClipboardManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import coil3.compose.AsyncImage
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.text.toPx
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.htmlrenderer.api.BlockNode
import io.element.android.libraries.htmlrenderer.api.CodeBlockNode
import io.element.android.libraries.htmlrenderer.api.DetailsNode
import io.element.android.libraries.htmlrenderer.api.DocumentNode
import io.element.android.libraries.htmlrenderer.api.HeaderNode
import io.element.android.libraries.htmlrenderer.api.HtmlMessageParser.Companion.INLINE_CODE_ANNOTATION_TAG
import io.element.android.libraries.htmlrenderer.api.HtmlMessageParser.Companion.LINK_ANNOTATION_TAG
import io.element.android.libraries.htmlrenderer.api.ImageNodeContent
import io.element.android.libraries.htmlrenderer.api.InlineContent
import io.element.android.libraries.htmlrenderer.api.ListNode
import io.element.android.libraries.htmlrenderer.api.MentionNodeContent
import io.element.android.libraries.htmlrenderer.api.ParagraphNode
import io.element.android.libraries.htmlrenderer.api.QuoteNode
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.ui.common.contentavoidinglayout.ContentAvoidingLayout
import io.element.android.libraries.ui.common.contentavoidinglayout.ContentAvoidingLayoutData
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
 * @param currentUserId used to style a mention of the current user differently; may be null.
 * @param onLinkClick invoked with the target URL when a link is tapped.
 * @param onLinkLongClick invoked with the target URL when a link is long-pressed.
 * @param onMentionClick invoked when a mention pill is tapped.
 * @param imageModel maps an [ImageNodeContent] to the model passed to [AsyncImage]. Defaults to the
 * raw image URL; the timeline can supply a mapper that turns `mxc://` URLs into a media request.
 * @param hideImages when true, inline images are replaced by a tappable grey placeholder until the
 * viewer reveals them individually; a revealed image's URL is added to [LocalAllowedInlineImages].
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
    imageModel: (ImageNodeContent) -> Any = { it.url },
    hideImages: Boolean = false,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit = {},
) {
    val measuredParagraph = remember(node) { node.lastBlockNode() }
    val context = remember(
        currentUserId,
        onLinkClick,
        onLinkLongClick,
        onMentionClick,
        imageModel,
        hideImages,
        measuredParagraph,
        onContentLayoutChange
    ) {
        RenderContext(
            currentUserId = currentUserId,
            onLinkClick = onLinkClick,
            onLinkLongClick = onLinkLongClick,
            onMentionClick = onMentionClick,
            imageModel = imageModel,
            hideImages = hideImages,
            lastBlockNode = measuredParagraph,
            onContentLayoutChange = onContentLayoutChange,
        )
    }
    // Each message keeps its own reveal state, unless a host provides a shared [LocalAllowedImages].
    val allowedImages = remember { AllowedImages() }
    CompositionLocalProvider(LocalAllowedInlineImages provides allowedImages) {
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
    val imageModel: (ImageNodeContent) -> Any,
    val hideImages: Boolean,
    // The latest descendant BlockNode of the root DocumentNode
    val lastBlockNode: BlockNode?,
    val onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
)

internal fun BlockNode.lastBlockNode(): BlockNode? = when (this) {
    is ParagraphNode -> this
    is DocumentNode -> children.lastOrNull()?.lastBlockNode()
    is ListNode -> items.lastOrNull()?.children?.lastOrNull()?.lastBlockNode()
    is QuoteNode -> this
    is CodeBlockNode -> this
    is HeaderNode -> this
    is DetailsNode -> this
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
    if (node === context.lastBlockNode && (node !is ParagraphNode && node !is HeaderNode)) {
        context.onContentLayoutChange(ContentAvoidingLayoutData.NotOverlapping)
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
        is DetailsNode -> DetailsView(node = node, context = context, modifier = modifier)
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
    inlineContent: ImmutableMap<String, InlineContent>,
    textStyle: TextStyle,
    context: RenderContext,
    modifier: Modifier = Modifier,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    val linkColor = ElementTheme.colors.textLinkExternal
    val codeBackgroundColor = ElementTheme.colors.bgSubtleSecondary
    val codeBorderColor = ElementTheme.colors.borderInteractiveSecondary
    val styledText = remember(text, linkColor) {
        text.applyLinkStyles(linkColor = linkColor)
    }
    val resolvedInlineContent = rememberInlineContent(inlineContent, context)
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
    1 -> ElementTheme.typography.fontHeadingXlBold
    2 -> ElementTheme.typography.fontHeadingLgBold
    3 -> ElementTheme.typography.fontHeadingMdBold
    4 -> ElementTheme.typography.fontHeadingSmMedium
    5 -> ElementTheme.typography.fontBodyLgMedium
    else -> ElementTheme.typography.fontBodyMdMedium
}

@Composable
private fun DetailsView(
    node: DetailsNode,
    context: RenderContext,
    modifier: Modifier = Modifier,
) {
    val expanded = remember { mutableStateOf(false) }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(BlockSpacing),
    ) {
        // The always-visible summary; tapping it toggles the hidden body.
        Row(
            modifier = Modifier.clickable { expanded.value = !expanded.value },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (expanded.value) CompoundIcons.ChevronDown() else CompoundIcons.ChevronRight(),
                contentDescription = null,
                tint = ElementTheme.colors.iconSecondary,
                modifier = Modifier.size(SpoilerChevronSize),
            )
            Spacer(modifier = Modifier.width(SpoilerChevronSpacing))
            TextBlock(
                text = node.summary,
                inlineContent = node.summaryInlineContent,
                textStyle = ElementTheme.typography.fontBodyMdMedium,
                context = context,
            )
        }
        if (expanded.value) {
            BlockNodes(nodes = node.children, context = context)
        }
    }
}

@Composable
private fun CodeBlockView(
    node: CodeBlockNode,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val clipboardManager = remember { context.getSystemService(ClipboardManager::class.java) }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = ElementTheme.colors.borderInteractiveSecondary, shape = RoundedCornerShape(8.dp))
            .background(ElementTheme.colors.bgSubtleSecondary)
            .height(IntrinsicSize.Min)
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    clipboardManager.setPrimaryClip(ClipData(newPlainText("Code", node.code)))
                },
                onLongClickLabel = "Copy code to clipboard",
            ),
    ) {
        Text(
            modifier = Modifier.horizontalScroll(scrollState).padding(horizontal = 12.dp, vertical = 8.dp),
            text = node.code,
            style = ElementTheme.typography.fontBodySmRegular.copy(fontFamily = FontFamily.Monospace),
            color = ElementTheme.colors.textPrimary,
            softWrap = false,
        )

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

@Composable
private fun InlineImage(
    content: ImageNodeContent,
    model: Any,
    hideImages: Boolean,
    modifier: Modifier = Modifier,
) {
    val allowedImages = LocalAllowedInlineImages.current
    val isRevealed = !hideImages || content.url in allowedImages
    if (isRevealed) {
        AsyncImage(
            model = model,
            contentDescription = content.alt,
            contentScale = ContentScale.Fit,
            modifier = modifier.fillMaxSize(),
        )
    } else {
        // Hidden by default: a tappable grey placeholder that reveals the image when clicked.
        Box(
            modifier = modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(4.dp))
                .background(ElementTheme.colors.bgSubtleSecondary)
                .clickable { allowedImages.allow(content.url) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(CompoundIcons.VisibilityOff(), contentDescription = "Show image")
        }
    }
}

/**
 * Builds an [InlineTextContent] entry for each inline-content placeholder. A mention pill is measured
 * so its placeholder fits the text; an image placeholder uses the `<img>` dimensions (capped at
 * [MaxInlineImageWidth]) or an emoji-sized square when they are missing.
 */
@Composable
private fun rememberInlineContent(
    inlineContent: ImmutableMap<String, InlineContent>,
    context: RenderContext,
): ImmutableMap<String, InlineTextContent> {
    if (inlineContent.isEmpty()) return persistentMapOf()
    val textMeasurer = rememberTextMeasurer()
    val pillTextStyle = ElementTheme.typography.fontBodyLgMedium
    val density = LocalDensity.current
    return inlineContent.mapValues { (_, item) ->
        when (item) {
            is MentionNodeContent -> {
                val measured = textMeasurer.measure(item.displayText, pillTextStyle)
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
                        mention = item,
                        isCurrentUser = item is MentionNodeContent.User && item.userId == context.currentUserId,
                        onClick = { context.onMentionClick(item) },
                    )
                }
            }
            is ImageNodeContent -> {
                val (imageWidth, imageHeight) = item.inlineImageSize()
                InlineTextContent(
                    placeholder = Placeholder(
                        width = with(density) { imageWidth.toSp() },
                        height = with(density) { imageHeight.toSp() },
                        placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
                    ),
                ) {
                    InlineImage(
                        content = item,
                        model = context.imageModel(item),
                        hideImages = context.hideImages,
                    )
                }
            }
        }
    }.toImmutableMap()
}

/**
 * The placeholder size for an inline image: the `<img>` dimensions (treated as dp, scaled down to
 * [MaxInlineImageWidth] preserving the aspect ratio), or an emoji-sized square when the dimensions
 * are missing or the image is a custom emoji.
 */
private fun ImageNodeContent.inlineImageSize(): Pair<Dp, Dp> {
    val width = width
    val height = height
    if (isEmoticon || width == null || height == null || width <= 0 || height <= 0) {
        return EmojiImageSize to EmojiImageSize
    }
    val widthDp = width.dp
    val heightDp = height.dp
    return if (widthDp <= MaxInlineImageWidth) {
        widthDp to heightDp
    } else {
        MaxInlineImageWidth to MaxInlineImageWidth * (height.toFloat() / width.toFloat())
    }
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

/** Size of an inline image that has no dimensions (or is a custom emoji): roughly a line's height. */
private val EmojiImageSize: Dp = 20.dp

/** Inline images are scaled down to at most this width, preserving their aspect ratio. */
private val MaxInlineImageWidth: Dp = 120.dp

/** The disclosure chevron shown before a spoiler ([DetailsNode]) summary, and its trailing gap. */
private val SpoilerChevronSize: Dp = 20.dp
private val SpoilerChevronSpacing: Dp = 4.dp

/** The width of the fading edge gradient drawn at the left and right of a [CodeBlockView]. */
private val CodeBlockFadingEdgeWidth: Dp = 32.dp
