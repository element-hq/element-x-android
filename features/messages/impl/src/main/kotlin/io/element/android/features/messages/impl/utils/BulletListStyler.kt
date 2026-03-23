/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.utils

import android.text.SpannableStringBuilder
import android.text.Spanned
import androidx.core.text.getSpans
import io.element.android.libraries.textcomposer.spans.OutlineBulletSpan
import io.element.android.wysiwyg.view.spans.UnorderedListSpan

private val gapWidthField = UnorderedListSpan::class.java.getDeclaredField("gapWidth").apply { isAccessible = true }
private val bulletRadiusField = UnorderedListSpan::class.java.getDeclaredField("bulletRadius").apply { isAccessible = true }

private fun UnorderedListSpan.getGapWidth(): Int = gapWidthField.getInt(this)
private fun UnorderedListSpan.getBulletRadius(): Int = bulletRadiusField.getInt(this)

fun CharSequence.replaceNestedBulletsWithOutlines(): CharSequence {
    val ssb = SpannableStringBuilder(this)
    val spans = ssb.getSpans<UnorderedListSpan>()
    if (spans.size <= 1) return this

    val spanInfo = spans.map { span ->
        Triple(span, ssb.getSpanStart(span), ssb.getSpanEnd(span))
    }

    for ((span, start, end) in spanInfo) {
        val isNested = spanInfo.any { (other, otherStart, otherEnd) ->
            other !== span && otherStart <= start && otherEnd >= end
        }
        if (isNested) {
            ssb.removeSpan(span)
            ssb.setSpan(
                OutlineBulletSpan(
                    gapWidth = span.getGapWidth(),
                    bulletRadius = span.getBulletRadius(),
                ),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
    }

    return ssb
}
