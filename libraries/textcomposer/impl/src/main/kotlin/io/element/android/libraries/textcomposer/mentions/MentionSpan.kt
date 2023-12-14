/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.textcomposer.mentions

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.style.ReplacementSpan
import kotlin.math.roundToInt

class MentionSpan(
    val type: Type,
    val backgroundColor: Int,
    val textColor: Int,
    val startPadding: Int,
    val endPadding: Int,
    val typeface: Typeface = Typeface.DEFAULT,
) : ReplacementSpan() {

    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        val mentionText = getActualText(text, start)
        var actualEnd = end
        if (mentionText != text.toString()) {
            actualEnd = end + 1
        }
        paint.typeface = typeface
        return paint.measureText(mentionText, start, actualEnd).roundToInt() + startPadding + endPadding
    }

    override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        val mentionText = getActualText(text, start)
        var actualEnd = end
        if (mentionText != text.toString()) {
            actualEnd = end + 1
        }
        val textWidth = paint.measureText(mentionText, start, actualEnd)
        // Extra vertical space to add below the baseline (y). This helps us center the span vertically
        val extraVerticalSpace = y + paint.ascent() + paint.descent() - top
        val rect = RectF(x, top.toFloat(), x + textWidth + startPadding + endPadding, y.toFloat() + extraVerticalSpace)
        paint.color = backgroundColor
        canvas.drawRoundRect(rect, rect.height() / 2, rect.height() / 2, paint)
        paint.color = textColor
        paint.typeface = typeface
        canvas.drawText(mentionText, start, actualEnd, x + startPadding, y.toFloat(), paint)
    }

    private fun getActualText(text: CharSequence?, start: Int): String {
        return when (type) {
            Type.USER -> {
                val mentionText = text.toString()
                if (start in mentionText.indices && mentionText[start] != '@') {
                    mentionText.replaceRange(start, start, "@")
                } else {
                    mentionText
                }
            }
            Type.ROOM -> {
                val mentionText = text.toString()
                if (start in mentionText.indices && mentionText[start] != '#') {
                    mentionText.replaceRange(start, start, "#")
                } else {
                    mentionText
                }
            }
        }
    }

    enum class Type {
        USER,
        ROOM,
    }
}
