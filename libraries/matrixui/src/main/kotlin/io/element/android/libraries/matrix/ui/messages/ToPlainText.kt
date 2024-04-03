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

import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.api.timeline.item.event.FormattedBody
import io.element.android.libraries.matrix.api.timeline.item.event.MessageFormat
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeVisitor

/**
 * Converts the HTML string in [TextMessageType.formatted] to a plain text representation by parsing it and removing all formatting.
 * If the message is not formatted or the format is not [MessageFormat.HTML], the [TextMessageType.body] is returned instead.
 */
fun TextMessageType.toPlainText(
    permalinkParser: PermalinkParser,
) = formatted?.toPlainText(permalinkParser) ?: body

/**
 * Converts the HTML string in [FormattedBody.body] to a plain text representation by parsing it and removing all formatting.
 * If the message is not formatted or the format is not [MessageFormat.HTML] we return `null`.
 * @param permalinkParser the parser to use to parse the mentions.
 * @param prefix if not null, the prefix will be inserted at the beginning of the message.
 */
fun FormattedBody.toPlainText(
    permalinkParser: PermalinkParser,
    prefix: String? = null,
): String? {
    return this.toHtmlDocument(
        permalinkParser = permalinkParser,
        prefix = prefix,
    )?.toPlainText()
}

/**
 * Converts the HTML [Document] to a plain text representation by parsing it and removing all formatting.
 */
fun Document.toPlainText(): String {
    val visitor = PlainTextNodeVisitor()
    traverse(visitor)
    return visitor.build()
}

private class PlainTextNodeVisitor : NodeVisitor {
    private val builder = StringBuilder()

    override fun head(node: Node, depth: Int) {
        if (node is TextNode && node.text().isNotBlank()) {
            builder.append(node.text())
        } else if (node is Element && node.tagName() == "li") {
            val index = node.elementSiblingIndex()
            val isOrdered = node.parent()?.nodeName()?.lowercase() == "ol"
            if (isOrdered) {
                builder.append("${index + 1}. ")
            } else {
                builder.append("• ")
            }
        } else if (node is Element && node.isBlock && builder.lastOrNull() != '\n') {
            builder.append("\n")
        }
    }

    override fun tail(node: Node, depth: Int) {
        fun nodeIsBlockButNotLastOne(node: Node) = node is Element && node.isBlock && node.lastElementSibling() !== node
        fun nodeIsLineBreak(node: Node) = node.nodeName().lowercase() == "br"
        if (nodeIsBlockButNotLastOne(node) || nodeIsLineBreak(node)) {
            builder.append("\n")
        }
    }

    fun build(): String {
        return builder.toString().trim()
    }
}
