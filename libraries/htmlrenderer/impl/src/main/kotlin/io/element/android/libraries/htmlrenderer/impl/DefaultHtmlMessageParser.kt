/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.impl

import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.htmlrenderer.api.BlockNode
import io.element.android.libraries.htmlrenderer.api.CodeBlockNode
import io.element.android.libraries.htmlrenderer.api.DocumentNode
import io.element.android.libraries.htmlrenderer.api.HtmlMessageParser
import io.element.android.libraries.htmlrenderer.api.HtmlMessageParser.Companion.LINK_ANNOTATION_TAG
import io.element.android.libraries.htmlrenderer.api.ListItemNode
import io.element.android.libraries.htmlrenderer.api.ListNode
import io.element.android.libraries.htmlrenderer.api.MentionNodeContent
import io.element.android.libraries.htmlrenderer.api.ParagraphNode
import io.element.android.libraries.htmlrenderer.api.QuoteNode
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

/**
 * Recursive-descent [HtmlMessageParser] over a (sanitized) jsoup [Document].
 *
 * Block-level elements become their matching [BlockNode]; consecutive inline nodes are
 * coalesced into a single [ParagraphNode] whose text is an [AnnotatedString].
 */
@ContributesBinding(AppScope::class)
class DefaultHtmlMessageParser(
    private val permalinkParser: PermalinkParser,
) : HtmlMessageParser {
    override fun parse(document: Document): DocumentNode {
        return DocumentNode(children = parseBlocks(document.body()))
    }

    /**
     * Walks the children of [parent], emitting a block node for each block-level element and
     * buffering runs of inline content into [ParagraphNode]s in between.
     */
    private fun parseBlocks(parent: Element): ImmutableList<BlockNode> {
        val blocks = mutableListOf<BlockNode>()
        val inlineBuffer = mutableListOf<Node>()

        fun flushInline() {
            if (inlineBuffer.isNotEmpty()) {
                buildParagraph(inlineBuffer)?.let(blocks::add)
                inlineBuffer.clear()
            }
        }

        for (node in parent.childNodes()) {
            when {
                // The fallback reply content is never rendered.
                node is Element && node.tagName() == TAG_MX_REPLY -> Unit
                node is Element && node.isBlockLevel() -> {
                    flushInline()
                    parseBlock(node)?.let(blocks::add)
                }
                else -> inlineBuffer.add(node)
            }
        }
        flushInline()
        return blocks.toImmutableList()
    }

    private fun parseBlock(element: Element): BlockNode? = when (element.tagName()) {
        TAG_P -> buildParagraph(element.childNodes())
        TAG_PRE -> CodeBlockNode(code = element.wholeText().trimEnd('\n'))
        TAG_BLOCKQUOTE -> QuoteNode(children = parseBlocks(element))
        TAG_UL -> buildList(element, ordered = false)
        TAG_OL -> buildList(element, ordered = true)
        else -> null
    }

    private fun buildList(element: Element, ordered: Boolean): ListNode {
        val startIndex = if (ordered) element.attr("start").toIntOrNull() ?: 1 else 1
        val items = element.childNodes()
            .filterIsInstance<Element>()
            .filter { it.tagName() == TAG_LI }
            .map { ListItemNode(children = parseBlocks(it)) }
            .toImmutableList()
        return ListNode(ordered = ordered, startIndex = startIndex, items = items)
    }

    /** Builds a [ParagraphNode] from a run of inline [nodes], or `null` if it holds no content. */
    private fun buildParagraph(nodes: List<Node>): ParagraphNode? {
        val builder = InlineContentBuilder()
        nodes.forEach { builder.append(it) }
        return builder.build()
    }

    /**
     * Accumulates inline nodes into an [AnnotatedString] plus a map of mention placeholders.
     * A fresh instance is used for each paragraph so placeholder ids stay local to it.
     */
    private inner class InlineContentBuilder {
        private val builder = AnnotatedString.Builder()
        private val mentions = mutableMapOf<String, MentionNodeContent>()
        private var mentionCount = 0

        fun append(node: Node) {
            when (node) {
                is TextNode -> builder.append(node.text())
                is Element -> appendElement(node)
                else -> Unit
            }
        }

        private fun appendElement(element: Element) {
            when (element.tagName()) {
                TAG_BR -> builder.append("\n")
                TAG_B, TAG_STRONG -> withStyle(SpanStyle(fontWeight = FontWeight.Bold), element)
                TAG_I, TAG_EM -> withStyle(SpanStyle(fontStyle = FontStyle.Italic), element)
                TAG_U -> withStyle(SpanStyle(textDecoration = TextDecoration.Underline), element)
                TAG_DEL -> withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), element)
                TAG_CODE -> withStyle(SpanStyle(fontFamily = FontFamily.Monospace), element)
                TAG_A -> appendLink(element)
                TAG_MX_REPLY -> Unit
                // Unknown inline wrapper: keep its text so nothing is dropped.
                else -> appendChildren(element)
            }
        }

        private fun appendChildren(element: Element) {
            element.childNodes().forEach { append(it) }
        }

        private fun withStyle(style: SpanStyle, element: Element) {
            val start = builder.length
            appendChildren(element)
            builder.addStyle(style, start, builder.length)
        }

        private fun appendLink(element: Element) {
            val href = element.attr("href")
            val mention = href.takeIf { it.isNotEmpty() }?.let { resolveMention(element, it) }
            if (mention != null) {
                val id = "$MENTION_ID_PREFIX${mentionCount++}"
                mentions[id] = mention
                builder.appendInlineContent(id, mention.displayText)
                return
            }
            val start = builder.length
            appendChildren(element)
            if (href.isNotEmpty() && builder.length > start) {
                builder.addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, builder.length)
                builder.addStringAnnotation(LINK_ANNOTATION_TAG, href, start, builder.length)
            }
        }

        private fun resolveMention(element: Element, href: String): MentionNodeContent? {
            return when (val permalink = permalinkParser.parse(href)) {
                is PermalinkData.UserLink -> MentionNodeContent.User(
                    displayText = element.text(),
                    userId = permalink.userId,
                )
                is PermalinkData.RoomLink -> if (permalink.eventId == null) {
                    MentionNodeContent.Room(
                        displayText = element.text(),
                        roomIdOrAlias = permalink.roomIdOrAlias,
                    )
                } else {
                    // A link to a specific event is a regular link, not a mention pill.
                    null
                }
                else -> null
            }
        }

        fun build(): ParagraphNode? {
            val text = builder.toAnnotatedString()
            if (text.isBlank() && mentions.isEmpty()) return null
            return ParagraphNode(
                text = text,
                inlineContent = mentions.toImmutableMap(),
            )
        }
    }
}

/** Tags that start a new block-level node. Anything else is treated as inline content. */
private fun Element.isBlockLevel(): Boolean = tagName() in BLOCK_TAGS

private val BLOCK_TAGS = setOf(TAG_P, TAG_PRE, TAG_BLOCKQUOTE, TAG_UL, TAG_OL)

private const val TAG_P = "p"
private const val TAG_PRE = "pre"
private const val TAG_BLOCKQUOTE = "blockquote"
private const val TAG_UL = "ul"
private const val TAG_OL = "ol"
private const val TAG_LI = "li"
private const val TAG_BR = "br"
private const val TAG_B = "b"
private const val TAG_STRONG = "strong"
private const val TAG_I = "i"
private const val TAG_EM = "em"
private const val TAG_U = "u"
private const val TAG_DEL = "del"
private const val TAG_CODE = "code"
private const val TAG_A = "a"
private const val TAG_MX_REPLY = "mx-reply"

private const val MENTION_ID_PREFIX = "mention_"
