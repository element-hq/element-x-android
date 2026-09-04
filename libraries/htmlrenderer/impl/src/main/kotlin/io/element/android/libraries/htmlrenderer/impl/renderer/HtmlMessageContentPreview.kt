/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.impl.renderer

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.htmlrenderer.api.BlockNode
import io.element.android.libraries.htmlrenderer.api.CodeBlockNode
import io.element.android.libraries.htmlrenderer.api.DocumentNode
import io.element.android.libraries.htmlrenderer.api.HtmlMessageParser.Companion.INLINE_CODE_ANNOTATION_TAG
import io.element.android.libraries.htmlrenderer.api.HtmlMessageParser.Companion.LINK_ANNOTATION_TAG
import io.element.android.libraries.htmlrenderer.api.ListItemNode
import io.element.android.libraries.htmlrenderer.api.ListNode
import io.element.android.libraries.htmlrenderer.api.MentionNodeContent
import io.element.android.libraries.htmlrenderer.api.ParagraphNode
import io.element.android.libraries.htmlrenderer.api.QuoteNode
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList

@PreviewsDayNight
@Composable
internal fun HtmlMessageContentPreview(
    @PreviewParameter(HtmlMessageContentProvider::class) node: DocumentNode,
) = ElementPreview {
    HtmlMessageContent(
        node = node,
        modifier = Modifier.padding(16.dp),
        currentUserId = UserId("@me:example.org"),
    )
}

internal class HtmlMessageContentProvider : PreviewParameterProvider<DocumentNode> {
    override val values: Sequence<DocumentNode>
        get() = sequenceOf(
            richParagraph(),
            inlineCode(),
            multilineInlineCode(),
            orderedList(),
            quote(),
            codeBlock(),
            mention(),
            nestedList(),
            nestedListWithComplexContents(),
        )

    private fun richParagraph() = document(
        paragraph(
            buildAnnotatedString {
                append("Hello ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("bold") }
                append(", ")
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append("italic") }
                append(" and a ")
                val start = length
                append("link")
                addStringAnnotation(LINK_ANNOTATION_TAG, "https://element.io", start, length)
            }
        ),
    )

    private fun inlineCode() = document(
        paragraph(
            buildAnnotatedString {
                append("Run ")
                val start = length
                withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) { append("git status") }
                addStringAnnotation(INLINE_CODE_ANNOTATION_TAG, "", start, length)
                append(" to see changes")
            }
        ),
    )

    private fun multilineInlineCode() = document(
        QuoteNode(persistentListOf(
            paragraph(
                buildAnnotatedString {
                    append("Trying ")
                    val start = length
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) { append("inline code when it needs to be wrapped in several lines, just to check what it looks like") }
                    addStringAnnotation(INLINE_CODE_ANNOTATION_TAG, "", start, length)
                    append(", is it good?")
                }
            ),
        ))
    )

    private fun orderedList() = document(
        ListNode(
            ordered = true,
            startIndex = 1,
            items = persistentListOf(
                ListItemNode(persistentListOf(paragraph(AnnotatedString("First item")))),
                ListItemNode(persistentListOf(paragraph(AnnotatedString("Second item")))),
            ),
        ),
    )

    private fun quote() = document(
        paragraph(AnnotatedString("Someone said:")),
        QuoteNode(persistentListOf(paragraph(AnnotatedString("This is a quoted message.")))),
    )

    private fun codeBlock() = document(
        CodeBlockNode("fun main() {\n    println(\"Hello, world!\")\n}"),
    )

    private fun mention() = document(
        paragraph(
            text = buildAnnotatedString {
                append("Hey ")
                appendInlineContent(MENTION_ID, "@alice")
                append(", welcome!")
            },
            inlineContent = persistentMapOf(
                MENTION_ID to MentionNodeContent.User(displayText = "@alice", userId = UserId("@alice:example.org")),
            ),
        ),
    )

    private fun nestedList() = document(
        ListNode(
            ordered = false,
            startIndex = 1,
            items = persistentListOf(
                ListItemNode(
                    persistentListOf(
                        paragraph(AnnotatedString("Parent item")),
                        ListNode(
                            ordered = false,
                            startIndex = 1,
                            items = persistentListOf(
                                ListItemNode(persistentListOf(paragraph(AnnotatedString("Nested item")))),
                            ),
                        ),
                    ),
                ),
            ),
        ),
    )

    private fun nestedListWithComplexContents() = document(
        ListNode(
            ordered = false,
            startIndex = 1,
            items = persistentListOf(
                ListItemNode(
                    persistentListOf(
                        paragraph(AnnotatedString("Parent item")),
                        ListNode(
                            ordered = false,
                            startIndex = 1,
                            items = persistentListOf(
                                ListItemNode(persistentListOf(
                                    paragraph(AnnotatedString("Nested item")),
                                    codeBlock(),
                                    quote(),
                                )),
                            ),
                        ),
                    ),
                ),
            ),
        ),
    )

    private fun document(vararg blocks: BlockNode) = DocumentNode(blocks.toList().toImmutableList())

    private fun paragraph(
        text: AnnotatedString,
        inlineContent: ImmutableMap<String, MentionNodeContent> = persistentMapOf(),
    ) = ParagraphNode(text = text, inlineContent = inlineContent)

    private companion object {
        const val MENTION_ID = "mention_0"
    }
}
