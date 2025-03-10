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
import android.text.style.ReplacementSpan
import androidx.core.text.getSpans
import io.element.android.libraries.core.extensions.orEmpty
import io.element.android.wysiwyg.view.spans.CustomMentionSpan
import kotlin.math.min
import kotlin.math.roundToInt

class MentionSpan(
    text: String,
    val rawValue: String,
    val type: Type,
) : ReplacementSpan() {
    companion object {
        private const val MAX_LENGTH = 20
    }

    var backgroundColor: Int = 0
    var textColor: Int = 0
    var startPadding: Int = 0
    var endPadding: Int = 0
    var typeface: Typeface = Typeface.DEFAULT

    private var textWidth = 0
    private val backgroundPaint = Paint().apply {
        isAntiAlias = true
        color = backgroundColor
    }

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
        backgroundPaint.color = backgroundColor
        val (startPaddingPx, endPaddingPx) = mentionSpanTheme.paddingValuesPx.value
        startPadding = startPaddingPx
        endPadding = endPaddingPx
        typeface = mentionSpanTheme.typeface.value
    }

    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        paint.typeface = typeface
        textWidth = paint.measureText(mentionText, 0, mentionText.length).roundToInt()
        return textWidth + startPadding + endPadding
    }

    override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        // Extra vertical space to add below the baseline (y). This helps us center the span vertically
        val extraVerticalSpace = y + paint.ascent() + paint.descent() - top

        val rect = RectF(x, top.toFloat(), x + textWidth + startPadding + endPadding, y.toFloat() + extraVerticalSpace)
        val radius = rect.height() / 2
        canvas.drawRoundRect(rect, radius, radius, backgroundPaint)
        paint.color = textColor
        paint.typeface = typeface
        canvas.drawText(mentionText, 0, mentionText.length, x + startPadding, y.toFloat(), paint)
    }

    private fun getActualText(text: String): CharSequence {
        return buildString {
            val mentionText = text.orEmpty()
            when (type) {
                Type.USER -> {
                    if (text.firstOrNull() != '@') {
                        append("@")
                    }
                }
                Type.ROOM -> {
                    if (text.firstOrNull() != '#') {
                        append("#")
                    }
                }
                Type.EVERYONE -> Unit
            }
            append(mentionText.substring(0, min(mentionText.length, MAX_LENGTH)))
            if (mentionText.length > MAX_LENGTH) {
                append("…")
            }
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
