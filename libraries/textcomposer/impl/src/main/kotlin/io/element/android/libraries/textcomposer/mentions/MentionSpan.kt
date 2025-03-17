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
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.wysiwyg.view.spans.CustomMentionSpan
import kotlin.math.roundToInt

/**
 * A span that represents a mention (user, room, etc.) in text.
 * 
 * @param originalText The original text that this span is replacing
 * @param type The type of mention this span represents
 */
class MentionSpan(
    val originalText: String,
    val type: MentionType,
) : ReplacementSpan() {

    var backgroundColor: Int = 0
    var textColor: Int = 0
    var startPadding: Int = 0
    var endPadding: Int = 0
    var typeface: Typeface = Typeface.DEFAULT

    private var measuredTextWidth = 0

    private val backgroundPaint = Paint()
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    // The formatted display text, will be set by the formatter
    var displayText: CharSequence = originalText
        private set

    /**
     * Updates the visual properties of this span.
     */
    fun update(mentionSpanTheme: MentionSpanTheme) {
        val isCurrentUser = when (type) {
            is MentionType.User -> type.userId == mentionSpanTheme.currentUserId
            else -> false
        }
        
        backgroundColor = when (type) {
            is MentionType.User -> if (isCurrentUser) mentionSpanTheme.currentUserBackgroundColor else mentionSpanTheme.otherBackgroundColor
            is MentionType.Everyone -> mentionSpanTheme.currentUserBackgroundColor
            is MentionType.Room -> mentionSpanTheme.otherBackgroundColor
            is MentionType.Message -> mentionSpanTheme.otherBackgroundColor
        }
        
        textColor = when (type) {
            is MentionType.User -> if (isCurrentUser) mentionSpanTheme.currentUserTextColor else mentionSpanTheme.otherTextColor
            is MentionType.Everyone -> mentionSpanTheme.currentUserTextColor
            is MentionType.Room -> mentionSpanTheme.otherTextColor
            is MentionType.Message -> mentionSpanTheme.otherTextColor
        }
        
        val (startPaddingPx, endPaddingPx) = mentionSpanTheme.paddingValuesPx.value
        startPadding = startPaddingPx
        endPadding = endPaddingPx
        typeface = mentionSpanTheme.typeface.value
    }

    /**
     * Updates the display text using a formatter.
     */
    fun updateDisplayText(formatter: MentionSpanFormatter) {
        displayText = formatter.formatDisplayText(type)
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
        measuredTextWidth = textPaint.measureText(displayText, 0, displayText.length).roundToInt()
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
                displayText,
                textPaint,
                availableWidthForText,
                TextUtils.TruncateAt.END
            )
        } else {
            displayText
        }
        canvas.drawText(textToDraw, 0, textToDraw.length, x + startPadding, y.toFloat(), textPaint)
    }
}

/**
 * Sealed class representing different types of mentions.
 */
sealed class MentionType {
    data class User(val userId: UserId) : MentionType()
    data class Room(val roomIdOrAlias: RoomIdOrAlias) : MentionType()
    data class Message(val roomIdOrAlias: RoomIdOrAlias, val eventId: EventId) : MentionType()
    data object Everyone : MentionType()
}

/**
 * Extension function to get all MentionSpans from a CharSequence.
 */
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
