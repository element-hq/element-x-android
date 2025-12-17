/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
            val index = node.elementSiblingIndex() + 1
            val isOrdered = node.parent()?.nodeName()?.lowercase() == "ol"
            if (isOrdered) {
                val startIndex = node.parent()?.attr("start")?.toIntOrNull()
                val actualIndex = if (startIndex != null) {
                    startIndex + index - 1
                } else {
                    index
                }
                builder.append("$actualIndex. ")
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
