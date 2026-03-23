/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan

class OutlineBulletSpan(
    private val gapWidth: Int,
    private val bulletRadius: Int,
) : LeadingMarginSpan {
    override fun getLeadingMargin(first: Boolean): Int {
        return 2 * bulletRadius + gapWidth
    }

    override fun drawLeadingMargin(
        canvas: Canvas,
        paint: Paint,
        x: Int,
        dir: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        isFirst: Boolean,
        layout: Layout,
    ) {
        if ((text as Spanned).getSpanStart(this) != start) return

        val originalStyle = paint.style
        val originalStrokeWidth = paint.strokeWidth

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = (bulletRadius / 4f).coerceAtLeast(1f)

        val textBounds = Rect()
        paint.getTextBounds("x", 0, 1, textBounds)
        val cy = baseline - textBounds.height() / 2f
        val cx = x + dir * bulletRadius

        canvas.drawCircle(cx.toFloat(), cy, bulletRadius.toFloat(), paint)

        paint.style = originalStyle
        paint.strokeWidth = originalStrokeWidth
    }
}
