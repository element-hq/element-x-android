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
 * @param prefix if not null, the prefix will be inserted at the beginning of the message.
 */
fun FormattedBody.toHtmlDocument(prefix: String? = null): Document? {
    return takeIf { it.format == MessageFormat.HTML }?.body?.let { formattedBody ->
        val dom = if (prefix != null) {
            Jsoup.parse("$prefix $formattedBody")
        } else {
            Jsoup.parse(formattedBody)
        }

        // Prepend `@` to mentions
        fixMentions(dom)

        dom
    }
}

private fun fixMentions(dom: Document) {
    val links = dom.getElementsByTag("a")
    links.forEach {
        if (it.hasAttr("href")) {
            val link = PermalinkParser.parse(it.attr("href"))
            if (link is PermalinkData.UserLink && !it.text().startsWith("@")) {
                it.prependText("@")
            }
        }
    }
}
