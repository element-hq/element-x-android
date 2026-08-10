/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Paint
import android.os.Build
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import android.text.style.LineHeightSpan
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.snackbar.LocalSnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.wysiwyg.view.spans.CodeBlockSpan
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.jsoup.nodes.Document

internal val CodeBlockHeaderHeight = 30.dp
internal val CodeBlockFooterHeight = 33.dp

private val COPY_ICON_SIZE = 20.dp
private val COPY_LABEL_SPACING = 8.dp
private val CODE_BLOCK_HORIZONTAL_INSET = 12.dp
private const val LANGUAGE_CLASS_PREFIX = "language-"
private const val FALLBACK_REPLY_NODE_TAG = "mx-reply"

/**
 * A code block found in a rendered message, together with the bounds of the box it is drawn in.
 *
 * The pixel values are in the coordinate space of the [Layout] the block was measured in, and
 * already include the space reserved by [withCodeBlockChrome].
 */
internal data class CodeBlockOverlay(
    val code: String,
    val language: String?,
    val blockLeftPx: Int,
    val blockTopPx: Int,
    val blockBottomPx: Int,
    val blockWidthPx: Int,
)

/**
 * The language of each code block in [document], in document order, or null where none is declared.
 *
 * The language only exists in the HTML (`<pre><code class="language-kotlin">`) and is dropped by the
 * time the DOM becomes spans, so it is read from the DOM and matched back up by position. A `pre`
 * inside the rich-reply fallback produces no span, so those are skipped to keep the match aligned.
 */
internal fun codeBlockLanguages(document: Document?): List<String?> {
    document ?: return emptyList()
    return document.select("pre")
        .filterNot { pre -> pre.parents().any { it.tagName() == FALLBACK_REPLY_NODE_TAG } }
        .map { pre ->
            pre.selectFirst("code")
                ?.classNames()
                ?.firstOrNull { it.startsWith(LANGUAGE_CLASS_PREFIX) }
                ?.removePrefix(LANGUAGE_CLASS_PREFIX)
                ?.takeIf { it.isNotBlank() }
        }
}

/**
 * Reserves room inside every code block for its header and copy row.
 *
 * A message is rendered by a single `TextView`, so the only way to make a block's own background
 * taller is to make its first and last lines taller. [LineHeightSpan]s do that without changing the
 * message's character offsets or what gets copied. The header is only reserved for blocks that
 * declare a language, so an unlabelled block does not gain an empty strip.
 */
internal fun withCodeBlockChrome(
    text: CharSequence,
    headerPx: Int,
    footerPx: Int,
    languages: List<String?>,
): CharSequence {
    if (!hasCodeBlock(text)) return text
    val spanned = text as Spanned
    val builder = SpannableStringBuilder(spanned)
    val spans = builder.getSpans(0, builder.length, CodeBlockSpan::class.java)
        .sortedBy { builder.getSpanStart(it) }
    for ((index, span) in spans.withIndex()) {
        val start = builder.getSpanStart(span)
        val end = builder.getSpanEnd(span)
        if (start >= end) continue
        builder.setSpan(
            CodeBlockChromeSpan(
                extraAscentPx = if (languages.getOrNull(index) != null) headerPx else 0,
                extraDescentPx = footerPx,
            ),
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
    }
    return builder
}

/** Whether [text] contains a code block at all. */
internal fun hasCodeBlock(text: CharSequence): Boolean {
    val spanned = text as? Spanned ?: return false
    return spanned.getSpans(0, spanned.length, CodeBlockSpan::class.java).isNotEmpty()
}

/**
 * Finds every code block in [text] and measures the box it is drawn in.
 *
 * The blocks are returned in document order, which is also how [languages] is matched back on.
 */
internal fun computeCodeBlockOverlays(
    text: CharSequence,
    layout: Layout,
    languages: List<String?> = emptyList(),
): ImmutableList<CodeBlockOverlay> {
    val spanned = text as? Spanned ?: return persistentListOf()
    return spanned.getSpans(0, spanned.length, CodeBlockSpan::class.java)
        .map { span -> spanned.getSpanStart(span) to spanned.getSpanEnd(span) }
        .sortedBy { (start, _) -> start }
        .mapIndexedNotNull { index, (start, end) ->
            if (start < 0 || end > spanned.length || start >= end) return@mapIndexedNotNull null
            val firstLine = layout.getLineForOffset(start)
            val lastLine = layout.getLineForOffset(end - 1)
            val marginPx = spanned.getSpans(start, end, LeadingMarginSpan::class.java)
                .filter { it !is CodeBlockSpan && spanned.getSpanStart(it) <= start && start < spanned.getSpanEnd(it) }
                .sumOf { it.getLeadingMargin(true) }
            val isRtl = layout.getParagraphDirection(firstLine) == Layout.DIR_RIGHT_TO_LEFT
            CodeBlockOverlay(
                code = spanned.subSequence(start, end).toString(),
                language = languages.getOrNull(index),
                blockLeftPx = if (isRtl) 0 else marginPx,
                blockTopPx = layout.getLineTop(firstLine),
                blockBottomPx = layout.getLineBottom(lastLine),
                blockWidthPx = layout.width - marginPx,
            )
        }
        .toImmutableList()
}

/**
 * Draws the chrome of a code block: a language label and separator at the top, and a copy row at the
 * bottom, both inside the block's own box and within the space [withCodeBlockChrome] reserved.
 *
 * The chrome's geometry is read from [latestOverlays] inside the placement and measure lambdas, not
 * from the composed [overlays]. The overlays are produced during the TextView's measure pass, one
 * phase after composition, so a compositional read would always draw the chrome one frame behind the
 * text while the bubble is animating. A layout-phase read of the same state sees the value the
 * TextView sibling has just written, keeping the chrome glued to the block in the same frame.
 */
@Composable
internal fun BoxScope.CodeBlockCopyButtons(
    overlays: ImmutableList<CodeBlockOverlay>,
    latestOverlays: () -> ImmutableList<CodeBlockOverlay>,
    onLongClick: (() -> Unit)?,
) {
    if (overlays.isEmpty()) return
    val context = LocalContext.current
    val snackbarDispatcher = LocalSnackbarDispatcher.current
    val copyLabel = stringResource(CommonStrings.action_copy)
    val fontScale = LocalDensity.current.fontScale
    for ((index, overlay) in overlays.withIndex()) {
        val separatorColor = ElementTheme.colors.borderInteractiveSecondary
        fun latest() = latestOverlays().getOrNull(index) ?: overlay

        if (overlay.language != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset { latest().let { IntOffset(x = it.blockLeftPx, y = it.blockTopPx) } }
                    .blockWidth { latest().blockWidthPx }
                    .height(CodeBlockHeaderHeight * fontScale),
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = CODE_BLOCK_HORIZONTAL_INSET),
                    text = overlay.language,
                    style = ElementTheme.typography.fontBodySmMedium,
                    color = ElementTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                HorizontalDivider(color = separatorColor)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset {
                    latest().let {
                        IntOffset(x = it.blockLeftPx, y = it.blockBottomPx - (CodeBlockFooterHeight * fontScale).roundToPx())
                    }
                }
                .blockWidth { latest().blockWidthPx }
                .height(CodeBlockFooterHeight * fontScale),
        ) {
            HorizontalDivider(color = separatorColor)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .combinedClickable(
                        role = Role.Button,
                        onClickLabel = copyLabel,
                        onLongClick = onLongClick,
                        onClick = {
                            context.getSystemService<ClipboardManager>()
                                ?.setPrimaryClip(ClipData.newPlainText("", overlay.code))
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                snackbarDispatcher.post(SnackbarMessage(CommonStrings.common_copied_to_clipboard))
                            }
                        },
                    ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.size(COPY_ICON_SIZE),
                    imageVector = CompoundIcons.Copy(),
                    contentDescription = null,
                    tint = ElementTheme.colors.iconSecondary,
                )
                Spacer(modifier = Modifier.width(COPY_LABEL_SPACING))
                Text(
                    text = copyLabel,
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = ElementTheme.colors.textSecondary,
                )
            }
        }
    }
}

/** Measures to a width only known at layout time, so the chrome can track the block within a frame. */
private fun Modifier.blockWidth(widthPx: () -> Int): Modifier = layout { measurable, constraints ->
    val width = widthPx().coerceAtLeast(0)
    val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
    layout(placeable.width, placeable.height) {
        placeable.place(0, 0)
    }
}

/**
 * Makes a code block's first line taller upward and its last line taller downward so its chrome has
 * somewhere to live.
 *
 * [LineHeightSpan] is a paragraph-affecting span: [Layout] asks every span in a paragraph about
 * every line of that paragraph, no matter how narrow the span's attachment range is, so which lines
 * to grow cannot be encoded in the attachment range — the span covers the whole block and picks its
 * lines here instead, by the character they hold.
 *
 * Growing a line is also not enough on its own. When a span run wraps, `StaticLayout` seeds the
 * next line with the previous line's chosen metrics ("preserve metrics for current span"), so the
 * header growth would silently repeat on the line after the one it was meant for. The span
 * remembers the exact metrics it produced by growing, and takes the growth back off any later line
 * that turns up with them.
 *
 * Known limit: the seeding min-folds away a wrapped line's own metrics, so a continuation line
 * whose glyphs need a taller ascent than the code font (an emoji, CJK) can be restored to the code
 * font's height instead of its own — a few px too tight. The line's real requirement is destroyed
 * before any span runs, so this cannot be told apart from a plain leak here.
 */
private class CodeBlockChromeSpan(
    private val extraAscentPx: Int,
    private val extraDescentPx: Int,
) : LineHeightSpan {
    private var grownAscent = Int.MIN_VALUE
    private var grownDescent = Int.MIN_VALUE

    override fun chooseHeight(
        text: CharSequence?,
        start: Int,
        end: Int,
        spanstartv: Int,
        lineHeight: Int,
        fm: Paint.FontMetricsInt?,
    ) {
        fm ?: return
        val spanned = text as? Spanned ?: return
        val blockStart = spanned.getSpanStart(this)
        val blockEnd = spanned.getSpanEnd(this)
        if (blockStart < 0 || blockEnd <= blockStart) return

        if (blockStart in start until end) {
            fm.ascent -= extraAscentPx
            fm.top -= extraAscentPx
            grownAscent = fm.ascent
        } else if (extraAscentPx > 0 && fm.ascent == grownAscent) {
            fm.ascent += extraAscentPx
            fm.top += extraAscentPx
        }

        if (blockEnd - 1 in start until end) {
            fm.descent += extraDescentPx
            fm.bottom += extraDescentPx
            grownDescent = fm.descent
        } else if (extraDescentPx > 0 && fm.descent == grownDescent) {
            fm.descent -= extraDescentPx
            fm.bottom -= extraDescentPx
        }
    }
}
