/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.mentions

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.ReplacementSpan
import androidx.core.text.getSpans
import io.element.android.libraries.core.extensions.orEmpty
import io.element.android.wysiwyg.view.spans.CustomMentionSpan
import kotlin.math.roundToInt

class MentionSpan(
    text: String,
    val rawValue: String,
    val type: Type,
) : ReplacementSpan() {

    var backgroundColor: Int = 0
    var textColor: Int = 0
    var startPadding: Int = 0
    var endPadding: Int = 0
    var typeface: Typeface = Typeface.DEFAULT

    private var measuredTextWidth = 0

    private val backgroundPaint = Paint()
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)


    var text: String = text
        set(value) {
            field = value
            mentionText = getActualText(text)
        }

    private var mentionText: CharSequence = getActualText(text)

    fun update(mentionSpanTheme: MentionSpanTheme) {
        val isCurrentUser = rawValue == mentionSpanTheme.currentUserId?.value
        backgroundColor = when (type) {
            Type.USER -> if (isCurrentUser) mentionSpanTheme.currentUserBackgroundColor else mentionSpanTheme.otherBackgroundColor
            Type.ROOM -> mentionSpanTheme.otherBackgroundColor
            Type.EVERYONE -> mentionSpanTheme.currentUserBackgroundColor
        }
        textColor = when (type) {
            Type.USER -> if (isCurrentUser) mentionSpanTheme.currentUserTextColor else mentionSpanTheme.otherTextColor
            Type.ROOM -> mentionSpanTheme.otherTextColor
            Type.EVERYONE -> mentionSpanTheme.currentUserTextColor
        }
        val (startPaddingPx, endPaddingPx) = mentionSpanTheme.paddingValuesPx.value
        startPadding = startPaddingPx
        endPadding = endPaddingPx
        typeface = mentionSpanTheme.typeface.value
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        textPaint.set(paint)
        textPaint.typeface = typeface
        // Measure the full text width without truncation
        measuredTextWidth = textPaint.measureText(mentionText, 0, mentionText.length).roundToInt()
        return measuredTextWidth + startPadding + endPadding
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        // Extra vertical space to add below the baseline (y). This helps us center the span vertically
        val extraVerticalSpace = y + paint.ascent() + paint.descent() - top

        val availableWidth = (canvas.width - x).coerceAtLeast(0f)
        val measuredWidth = measuredTextWidth + startPadding + endPadding
        val pillWidth = minOf(availableWidth, measuredWidth.toFloat())

        backgroundPaint.color = backgroundColor
        val rect = RectF(x, top.toFloat(), x + pillWidth, y.toFloat() + extraVerticalSpace)
        val radius = rect.height() / 2
        canvas.drawRoundRect(rect, radius, radius, backgroundPaint)

        textPaint.set(paint)
        textPaint.color = textColor
        textPaint.typeface = typeface

        val availableWidthForText = availableWidth - startPadding - endPadding
        val textToDraw = if (measuredTextWidth > availableWidthForText) {
            TextUtils.ellipsize(
                mentionText,
                textPaint,
                availableWidthForText,
                TextUtils.TruncateAt.END
            )
        } else {
            mentionText
        }
        canvas.drawText(textToDraw, 0, textToDraw.length, x + startPadding, y.toFloat(), textPaint)
    }

    private fun getActualText(text: String): CharSequence {
        return buildString {
            val mentionText = text.orEmpty()
            when (type) {
                Type.USER -> {
                    if (mentionText.firstOrNull() != '@') {
                        append("@")
                    }
                }
                Type.ROOM -> {
                    if (mentionText.firstOrNull() != '#') {
                        append("#")
                    }
                }
                Type.EVERYONE -> Unit
            }
            append(mentionText)
        }
    }

    enum class Type {
        USER,
        ROOM,
        EVERYONE,
    }
}

fun CharSequence.getMentionSpans(): List<MentionSpan> {
    return if (this is android.text.Spanned) {
        val customMentionSpans = getSpans<CustomMentionSpan>()
        if (customMentionSpans.isNotEmpty()) {
            // If we have custom mention spans created by the RTE, we need to extract the provided spans and filter them
            customMentionSpans.map { it.providedSpan }.filterIsInstance<MentionSpan>()
        } else {
            // Otherwise try to get the spans directly
            getSpans<MentionSpan>().toList()
        }
    } else {
        emptyList()
    }
}
