/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.messages

import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.api.timeline.item.event.FormattedBody
import io.element.android.libraries.matrix.api.timeline.item.event.MessageFormat
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Converts the HTML string [FormattedBody.body] to a [Document] by parsing it.
 * If the message is not formatted or the format is not [MessageFormat.HTML] we return `null`.
 *
 * This will also make sure mentions are prefixed with `@`.
 *
 * @param permalinkParser the parser to use to parse the mentions.
 * @param prefix if not null, the prefix will be inserted at the beginning of the message.
 */
fun FormattedBody.toHtmlDocument(
    permalinkParser: PermalinkParser,
    prefix: String? = null,
): Document? {
    return takeIf { it.format == MessageFormat.HTML }?.body
        // Trim whitespace at the end to avoid having wrong rendering of the message.
        // We don't trim the start in case it's used as indentation.
        ?.trimEnd()
        ?.let { formattedBody ->
            val dom = if (prefix != null) {
                Jsoup.parse("$prefix $formattedBody")
            } else {
                Jsoup.parse(formattedBody)
            }

            // Prepend `@` to mentions
            fixMentions(dom, permalinkParser)

            dom
        }
}

private fun fixMentions(
    dom: Document,
    permalinkParser: PermalinkParser,
) {
    val links = dom.getElementsByTag("a")
    links.forEach {
        if (it.hasAttr("href")) {
            val link = permalinkParser.parse(it.attr("href"))
            if (link is PermalinkData.UserLink && !it.text().startsWith("@")) {
                it.prependText("@")
            }
        }
    }
}
