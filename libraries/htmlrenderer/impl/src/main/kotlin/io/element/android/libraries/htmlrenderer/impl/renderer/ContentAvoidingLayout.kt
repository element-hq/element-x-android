/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.impl.renderer

import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.ResolvedTextDirection
import kotlin.math.roundToInt

/**
 * Data describing the measured layout of rendered content, used by the timeline to place the
 * message timestamp so it avoids (or overlaps) the content.
 *
 * NOTE: this intentionally mirrors `ContentAvoidingLayoutData` in `features/messages/impl`. It is
 * duplicated here so this library does not depend on the feature module; the two should be
 * consolidated (e.g. moved to a shared module) when the renderer is wired into the timeline.
 *
 * @param contentWidth The full width of the content in pixels.
 * @param contentHeight The full height of the content in pixels.
 * @param nonOverlappingContentWidth The width of the part of the content that can't overlap with the timestamp.
 * @param nonOverlappingContentHeight The height of the part of the content that can't overlap with the timestamp.
 */
data class ContentAvoidingLayoutData(
    val contentWidth: Int = 0,
    val contentHeight: Int = 0,
    val nonOverlappingContentWidth: Int = contentWidth,
    val nonOverlappingContentHeight: Int = contentHeight,
)

internal object ContentAvoidingLayout {
    /**
     * Builds an `onTextLayout` callback that measures the last line of a [TextLayoutResult] and
     * reports it through [onContentLayoutChange]. Mirrors `ContentAvoidingLayout.measureLastTextLine`
     * in `features/messages/impl`.
     */
    fun measureLastTextLine(
        onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
    ): (TextLayoutResult) -> Unit = { textLayout ->
        val textDirection = runCatching { textLayout.getParagraphDirection(0) }.getOrNull()
        val lastLine = textLayout.lineCount - 1
        val lastLineWidth = when (textDirection) {
            ResolvedTextDirection.Rtl -> textLayout.getLineLeft(lastLine).roundToInt()
            else -> textLayout.getLineRight(lastLine).roundToInt()
        }
        val lastLineHeight = textLayout.getLineBottom(lastLine).roundToInt()
        onContentLayoutChange(
            ContentAvoidingLayoutData(
                contentWidth = textLayout.size.width,
                contentHeight = textLayout.size.height,
                nonOverlappingContentWidth = lastLineWidth,
                nonOverlappingContentHeight = lastLineHeight,
            )
        )
    }
}
