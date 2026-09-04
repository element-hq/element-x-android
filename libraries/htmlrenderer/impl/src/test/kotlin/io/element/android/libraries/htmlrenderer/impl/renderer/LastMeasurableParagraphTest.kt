/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.impl.renderer

import androidx.compose.ui.text.AnnotatedString
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.htmlrenderer.api.CodeBlockNode
import io.element.android.libraries.htmlrenderer.api.DocumentNode
import io.element.android.libraries.htmlrenderer.api.ListItemNode
import io.element.android.libraries.htmlrenderer.api.ListNode
import io.element.android.libraries.htmlrenderer.api.ParagraphNode
import io.element.android.libraries.htmlrenderer.api.QuoteNode
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import org.junit.Test

class LastMeasurableParagraphTest {
    @Test
    fun `when the last block is a paragraph, it is measured`() {
        val last = paragraph("Last")
        val document = DocumentNode(persistentListOf(paragraph("First"), last))
        assertThat(document.lastMeasurableParagraph()).isSameInstanceAs(last)
    }

    @Test
    fun `when the last block is a code block, nothing is measured`() {
        val document = DocumentNode(persistentListOf(paragraph("Text"), CodeBlockNode("code")))
        assertThat(document.lastMeasurableParagraph()).isNull()
    }

    @Test
    fun `when the last block is a quote, nothing is measured`() {
        val document = DocumentNode(
            persistentListOf(
                paragraph("Text"),
                QuoteNode(persistentListOf(paragraph("Quoted"))),
            )
        )
        assertThat(document.lastMeasurableParagraph()).isNull()
    }

    @Test
    fun `the last paragraph of the last list item is measured`() {
        val last = paragraph("Second item")
        val document = DocumentNode(
            persistentListOf(
                ListNode(
                    ordered = false,
                    startIndex = 1,
                    items = persistentListOf(
                        ListItemNode(persistentListOf(paragraph("First item"))),
                        ListItemNode(persistentListOf(last)),
                    ),
                ),
            )
        )
        assertThat(document.lastMeasurableParagraph()).isSameInstanceAs(last)
    }

    @Test
    fun `a quote nested at the end of a list item is not measured`() {
        val document = DocumentNode(
            persistentListOf(
                ListNode(
                    ordered = false,
                    startIndex = 1,
                    items = persistentListOf(
                        ListItemNode(
                            persistentListOf(
                                paragraph("Item"),
                                QuoteNode(persistentListOf(paragraph("Quoted in item"))),
                            )
                        ),
                    ),
                ),
            )
        )
        assertThat(document.lastMeasurableParagraph()).isNull()
    }

    @Test
    fun `an empty document measures nothing`() {
        assertThat(DocumentNode(persistentListOf()).lastMeasurableParagraph()).isNull()
    }

    private fun paragraph(text: String): ParagraphNode = ParagraphNode(
        text = AnnotatedString(text),
        inlineContent = persistentMapOf(),
    )
}
