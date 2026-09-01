/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.messages

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.api.timeline.item.event.FormattedBody
import io.element.android.libraries.matrix.api.timeline.item.event.MessageFormat
import io.element.android.libraries.matrix.api.timeline.item.event.MessageTypeWithAttachment
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
 * Converts the HTML string in [MessageTypeWithAttachment.formattedCaption] to a plain text representation by parsing it and removing all formatting.
 * If the caption is not formatted or the format is not [MessageFormat.HTML], the [MessageTypeWithAttachment.caption] is returned instead.
 * If there is no caption, returns [default].
 */
fun MessageTypeWithAttachment.toPlainText(
    permalinkParser: PermalinkParser,
    default: String = filename,
): String {
    val plainTextFromFormatted = formattedCaption?.toPlainText(permalinkParser)
    return plainTextFromFormatted ?: caption ?: default
}

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
 * Converts the HTML string in [TextMessageType.formatted] to a text representation keeping the inline formatting, such as bold or strikethrough.
 * If the message is not formatted or the format is not [MessageFormat.HTML], the [TextMessageType.body] is returned instead.
 */
fun TextMessageType.toAnnotatedText(
    permalinkParser: PermalinkParser,
): CharSequence = formatted?.toHtmlDocument(permalinkParser = permalinkParser)?.toAnnotatedText() ?: body

/**
 * Converts the HTML [Document] to a plain text representation by parsing it and removing all formatting.
 */
fun Document.toPlainText(): String = toAnnotatedText().text

/**
 * Converts the HTML [Document] to a text representation which keeps the inline formatting as spans.
 */
fun Document.toAnnotatedText(): AnnotatedString {
    val visitor = AnnotatedTextNodeVisitor()
    traverse(visitor)
    return visitor.build()
}

private const val FALLBACK_REPLY_NODE_TAG = "mx-reply"

private fun Element.spanStyle(): SpanStyle? = when (tagName().lowercase()) {
    "del", "s", "strike" -> SpanStyle(textDecoration = TextDecoration.LineThrough)
    "u" -> SpanStyle(textDecoration = TextDecoration.Underline)
    "b", "strong" -> SpanStyle(fontWeight = FontWeight.Bold)
    "i", "em" -> SpanStyle(fontStyle = FontStyle.Italic)
    "code" -> SpanStyle(fontFamily = FontFamily.Monospace)
    else -> null
}

private class AnnotatedTextNodeVisitor : NodeVisitor {
    private val builder = AnnotatedString.Builder()
    private var lastChar: Char? = null

    override fun head(node: Node, depth: Int) {
        if (node is TextNode) {
            // If the text node is blank, only add a single whitespace char if there wasn't already one
            if (node.text().isBlank()) {
                if (lastChar?.isWhitespace() == false) {
                    append(" ")
                }
            } else {
                append(node.text())
            }
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
                append("$actualIndex. ")
            } else {
                append("• ")
            }
        } else if (node is Element && node.tagName() == FALLBACK_REPLY_NODE_TAG) {
            // Remove the fallback reply node and its contents so they aren't added to the plain text message
            node.remove()
        } else if (node is Element && node.isBlock && lastChar != '\n') {
            append("\n")
        }
        (node as? Element)?.spanStyle()?.let { builder.pushStyle(it) }
    }

    override fun tail(node: Node, depth: Int) {
        fun nodeIsBlockButNotLastOne(node: Node) = node is Element && node.isBlock && node.lastElementSibling() !== node
        fun nodeIsLineBreak(node: Node) = node.nodeName().lowercase() == "br"
        if ((node as? Element)?.spanStyle() != null) {
            builder.pop()
        }
        if (nodeIsBlockButNotLastOne(node) || nodeIsLineBreak(node)) {
            append("\n")
        }
    }

    private fun append(text: String) {
        if (text.isEmpty()) return
        builder.append(text)
        lastChar = text.last()
    }

    fun build(): AnnotatedString {
        val annotatedString = builder.toAnnotatedString()
        val start = annotatedString.text.indexOfFirst { !it.isWhitespace() }
        if (start == -1) return AnnotatedString("")
        val end = annotatedString.text.indexOfLast { !it.isWhitespace() } + 1
        return annotatedString.subSequence(start, end)
    }
}
