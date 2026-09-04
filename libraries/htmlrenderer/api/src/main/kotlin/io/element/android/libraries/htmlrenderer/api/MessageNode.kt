/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.api

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

/**
 * A node in the parsed representation of a formatted (HTML) message body.
 *
 * The tree is produced by [HtmlMessageParser] and consumed by a Compose renderer.
 * Block-level nodes ([BlockNode]) either contain other block nodes (nested lists, a
 * quote containing paragraphs, ...) or terminate in a [ParagraphNode] / [CodeBlockNode]
 * that carries the actual text.
 */
@Immutable
sealed interface MessageNode

/** A block-level node, laid out vertically by the renderer. */
@Immutable
sealed interface BlockNode : MessageNode

/**
 * The root of a parsed message: an ordered list of block-level children.
 */
@Immutable
data class DocumentNode(
    val children: ImmutableList<BlockNode>,
) : BlockNode

/**
 * A run of inline content (text with inline formatting) already flattened to an
 * [AnnotatedString]. This is the only leaf that carries styled text.
 *
 * Inline formatting (`<b>`, `<i>`, `<u>`, `<del>`, inline `<code>`) is encoded as
 * [androidx.compose.ui.text.SpanStyle] ranges, and links (`<a>`) as string annotations
 * tagged [HtmlMessageParser.LINK_ANNOTATION_TAG].
 *
 * Mentions and inline images are encoded as inline-content placeholders inside [text] (see
 * [androidx.compose.foundation.text.appendInlineContent]); [inlineContent] maps each placeholder id
 * to the [InlineContent] (a [MentionNodeContent] pill or an [ImageNodeContent] image) the renderer draws.
 */
@Immutable
data class ParagraphNode(
    val text: AnnotatedString,
    val inlineContent: ImmutableMap<String, InlineContent> = persistentMapOf(),
) : BlockNode

/**
 * A preformatted code block (`<pre>`). Rendered in a monospaced, horizontally
 * scrollable container. The text is kept raw: no inline styling is applied inside it.
 */
@Immutable
data class CodeBlockNode(
    val code: String,
) : BlockNode

/**
 * A block quote (`<blockquote>`). Quotes can nest and can contain any block content.
 */
@Immutable
data class QuoteNode(
    val children: ImmutableList<BlockNode>,
) : BlockNode

/**
 * An ordered (`<ol>`) or unordered (`<ul>`) list.
 *
 * @param ordered whether the list is numbered (`<ol>`) or bulleted (`<ul>`).
 * @param startIndex the number the first item of an ordered list starts at (honors `ol[start]`, default 1).
 * @param items the list items, in document order.
 */
@Immutable
data class ListNode(
    val ordered: Boolean,
    val startIndex: Int,
    val items: ImmutableList<ListItemNode>,
) : BlockNode

/**
 * A single list item (`<li>`). Being a block container, an item can itself hold
 * paragraphs, nested lists or quotes.
 */
@Immutable
data class ListItemNode(
    val children: ImmutableList<BlockNode>,
) : MessageNode
