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
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import org.jsoup.nodes.Document
import java.net.URI

internal fun findFirstPreviewableUrl(
    formattedBody: CharSequence,
    htmlDocument: Document?,
    permalinkParser: PermalinkParser,
): String? {
    val textUrls = formattedBody.extractUrlSpans()
        .ifEmpty { extractRawTextUrls(formattedBody.toString()) }
    val htmlUrls = htmlDocument
        ?.select("a[href]")
        ?.map { it.attr("href") }
        .orEmpty()
    return (textUrls + htmlUrls).firstOrNull { isPreviewableUrl(it, permalinkParser) }
}

internal fun isPreviewableUrl(url: String, permalinkParser: PermalinkParser): Boolean {
    val scheme = tryOrNull { URI(url).scheme?.lowercase() }
    if (scheme !in setOf("http", "https")) return false
    // Matrix mentions/permalinks (matrix.to and custom bases) render as https URLSpans;
    // skip anything the SDK resolves to a Matrix identifier.
    return permalinkParser.parse(url) is PermalinkData.FallbackLink
}

internal fun hostNameFromUrl(url: String): String {
    return tryOrNull { URI(url).host.orEmpty().removePrefix("www.") }
        ?.takeIf { it.isNotBlank() }
        ?: url
}

private fun CharSequence.extractUrlSpans(): List<String> {
    val spanned = this as? Spanned ?: return emptyList()
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
