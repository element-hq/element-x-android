/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.urlpreview

import android.text.Spanned
import android.text.style.URLSpan
import androidx.core.text.toSpannable
import io.element.android.libraries.core.data.tryOrNull
import org.jsoup.nodes.Document
import java.net.URI

internal fun findFirstPreviewableUrl(
    formattedBody: CharSequence,
    htmlDocument: Document?,
): String? {
    val textUrls = formattedBody.extractUrlSpans()
        .ifEmpty { extractRawTextUrls(formattedBody.toString()) }
    val htmlUrls = htmlDocument
        ?.select("a[href]")
        ?.map { it.attr("href") }
        .orEmpty()
    return (textUrls + htmlUrls).firstOrNull(::isPreviewableUrl)
}

internal fun isPreviewableUrl(url: String): Boolean {
    return tryOrNull { URI(url).scheme?.lowercase() } in setOf("http", "https")
}

internal fun hostNameFromUrl(url: String): String {
    return tryOrNull { URI(url).host.orEmpty().removePrefix("www.") }
        ?.takeIf { it.isNotBlank() }
        ?: url
}

private fun CharSequence.extractUrlSpans(): List<String> {
    val spanned = this as? Spanned ?: toSpannable() ?: return emptyList()
    return spanned.getSpans(0, spanned.length, URLSpan::class.java)
        .orEmpty()
        .sortedBy { spanned.getSpanStart(it) }
        .map { it.url }
}

private val rawUrlRegex = Regex("""https?://\S+""", RegexOption.IGNORE_CASE)

private fun extractRawTextUrls(text: String): List<String> {
    return rawUrlRegex.findAll(text)
        .map { matchResult -> matchResult.value.trimEnd('.', ',', ';', ':', '!', '?', ')', ']', '}') }
        .toList()
}
