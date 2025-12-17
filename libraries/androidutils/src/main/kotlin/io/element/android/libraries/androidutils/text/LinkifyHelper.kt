/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.text

import android.text.Spannable
import android.text.style.URLSpan
import android.text.util.Linkify
import androidx.core.text.getSpans
import androidx.core.text.toSpannable
import androidx.core.text.util.LinkifyCompat
import io.element.android.libraries.core.extensions.runCatchingExceptions
import timber.log.Timber
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * Helper class to linkify text while preserving existing URL spans.
 *
 * It also checks the linkified results to make sure URLs spans are not including trailing punctuation.
 */
object LinkifyHelper {
    fun linkify(
        text: CharSequence,
        @LinkifyCompat.LinkifyMask linkifyMask: Int = Linkify.WEB_URLS or Linkify.PHONE_NUMBERS or Linkify.EMAIL_ADDRESSES,
    ): CharSequence {
        // Convert the text to a Spannable to be able to add URL spans, return the original text if it's not possible (in tests, i.e.)
        val spannable = text.toSpannable() ?: return text

        // Get all URL spans, as they will be removed by LinkifyCompat.addLinks
        val oldURLSpans = spannable.getSpans<URLSpan>(0, text.length).associateWith {
            val start = spannable.getSpanStart(it)
            val end = spannable.getSpanEnd(it)
            Pair(start, end)
        }
        // Find and set as URLSpans any links present in the text
        val addedNewLinks = LinkifyCompat.addLinks(spannable, linkifyMask)

        // Process newly added URL spans
        if (addedNewLinks) {
            val newUrlSpans = spannable.getSpans<URLSpan>(0, spannable.length)
            for (urlSpan in newUrlSpans) {
                val start = spannable.getSpanStart(urlSpan)
                val end = spannable.getSpanEnd(urlSpan)

                // Try to avoid including trailing punctuation in the link.
                // Since this might fail in some edge cases, we catch the exception and just use the original end index.
                val newEnd = runCatchingExceptions {
                    adjustLinkifiedUrlSpanEndIndex(spannable, start, end)
                }.onFailure {
                    Timber.e(it, "Failed to adjust end index for link span")
                }.getOrNull() ?: end

                // Adapt the url in the URL span to the new end index too if needed
                if (end != newEnd) {
                    val url = spannable.subSequence(start, newEnd).toString()
                    spannable.removeSpan(urlSpan)
                    spannable.setSpan(URLSpan(url), start, newEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else {
                    spannable.setSpan(urlSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }

        // Restore old spans, remove new ones if there is a conflict
        for ((urlSpan, location) in oldURLSpans) {
            val (start, end) = location
            val addedConflictingSpans = spannable.getSpans<URLSpan>(start, end)
            if (addedConflictingSpans.isNotEmpty()) {
                for (span in addedConflictingSpans) {
                    spannable.removeSpan(span)
                }
            }

            spannable.setSpan(urlSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return spannable
    }

    private fun adjustLinkifiedUrlSpanEndIndex(spannable: Spannable, start: Int, end: Int): Int {
        var end = end

        // Trailing punctuation found, adjust the end index
        while (spannable[end - 1] in sequenceOf('.', ',', ';', ':', '!', '?', '…') && end > start) {
            end--
        }

        // If the last character is a closing parenthesis, check if it's part of a pair
        if (spannable[end - 1] == ')' && end > start) {
            val linkifiedTextLastPath = spannable.substring(start, end).substringAfterLast('/')
            val closingParenthesisCount = linkifiedTextLastPath.count { it == ')' }
            val openingParenthesisCount = linkifiedTextLastPath.count { it == '(' }
            // If it's not part of a pair, remove it from the link span by adjusting the end index
            end -= closingParenthesisCount - openingParenthesisCount
        }
        return end
    }
}

/**
 * Linkify the text with the default mask (WEB_URLS, PHONE_NUMBERS, EMAIL_ADDRESSES).
 */
fun CharSequence.safeLinkify(): CharSequence {
    return LinkifyHelper.linkify(this, Linkify.WEB_URLS or Linkify.PHONE_NUMBERS or Linkify.EMAIL_ADDRESSES)
}
