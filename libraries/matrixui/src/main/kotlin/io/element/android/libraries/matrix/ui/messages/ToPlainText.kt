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

import io.element.android.libraries.matrix.api.core.MatrixPatterns
import io.element.android.libraries.matrix.api.timeline.item.event.FormattedBody
import io.element.android.libraries.matrix.api.timeline.item.event.MessageFormat
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.toolbox.api.strings.StringProvider
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeVisitor

/**
 * Converts the HTML string in [TextMessageType.formatted] to a plain text representation by parsing it and removing all formatting.
 * If the message is not formatted or the format is not [MessageFormat.HTML], the [TextMessageType.body] is returned instead.
 */
fun TextMessageType.toPlainText(stringProvider: StringProvider? = null) =
    formatted?.toPlainText(stringProvider = stringProvider) ?: body

/**
 * Converts the HTML string in [FormattedBody.body] to a plain text representation by parsing it and removing all formatting.
 * If the message is not formatted or the format is not [MessageFormat.HTML] we return `null`.
 * @param prefix if not null, the prefix will be inserted at the beginning of the message.
 * @param stringProvider if not null, the string provider will be used to resolve [CommonStrings].common_in_reply_to string.
 */
fun FormattedBody.toPlainText(
    prefix: String? = null,
    stringProvider: StringProvider? = null,
): String? {
    return this.toHtmlDocument(prefix)?.toPlainText(stringProvider)
}

/**
 * Converts the HTML [Document] to a plain text representation by parsing it and removing all formatting.
 */
fun Document.toPlainText(
    stringProvider: StringProvider? = null,
): String {
    val visitor = PlainTextNodeVisitor(stringProvider)
    traverse(visitor)
    return visitor.build()
}

private class PlainTextNodeVisitor(
    private val stringProvider: StringProvider?
) : NodeVisitor {
    private val builder = StringBuilder()
    private var isInInMxReply = false

    override fun head(node: Node, depth: Int) {
        when {
            node is TextNode && node.text().isNotBlank() -> {
                val text = node.text()
                if (isInInMxReply) {
                    when {
                        MatrixPatterns.isUserId(text) -> {
                            builder.append(stringProvider?.getString(CommonStrings.common_in_reply_to, text) ?: "In reply to $text")
                        }
                    }
                } else {
                    builder.append(text)
                }
            }
            node is Element && node.tagName() == "li" -> {
                val index = node.elementSiblingIndex()
                val isOrdered = node.parent()?.nodeName()?.lowercase() == "ol"
                if (isOrdered) {
                    builder.append("${index + 1}. ")
                } else {
                    builder.append("• ")
                }
            }
            node is Element && node.isBlock && builder.lastOrNull() != '\n' -> {
                builder.append("\n")
            }
            node is Element && node.tagName() == "mx-reply" -> {
                isInInMxReply = true
            }
        }
    }

    override fun tail(node: Node, depth: Int) {
        when {
            isInInMxReply -> {
                if (node is Element && node.tagName() == "mx-reply") {
                    isInInMxReply = false
                    builder.append(": ")
                }
            }
            // Node is block but not last one
            node is Element && node.isBlock && node.lastElementSibling() !== node -> {
                builder.append("\n")
            }
            // Node is line break
            node.nodeName().lowercase() == "br" -> {
                builder.append("\n")
            }
        }
    }

    fun build(): String {
        return builder.toString().trim()
    }
}
