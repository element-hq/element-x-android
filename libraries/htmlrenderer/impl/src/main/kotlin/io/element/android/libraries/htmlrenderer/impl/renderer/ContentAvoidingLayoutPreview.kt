/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.impl.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.htmlrenderer.api.CodeBlockNode
import io.element.android.libraries.htmlrenderer.api.DocumentNode
import io.element.android.libraries.htmlrenderer.api.ListItemNode
import io.element.android.libraries.htmlrenderer.api.ListNode
import io.element.android.libraries.htmlrenderer.api.ParagraphNode
import io.element.android.libraries.htmlrenderer.api.QuoteNode
import kotlinx.collections.immutable.persistentListOf

private val documentNode = DocumentNode(
    persistentListOf(
        ParagraphNode(AnnotatedString("Hello World, this is a")),
        QuoteNode(persistentListOf(ParagraphNode(AnnotatedString("Quote with a very long text that wraps")))),
        CodeBlockNode("fun main() {\n    println(\"Hello, world!\")\n}"),
        ListNode(
            ordered = true,
            startIndex = 0,
            items = persistentListOf(
                ListItemNode(persistentListOf(ParagraphNode(AnnotatedString("First item")))),
                ListItemNode(persistentListOf(ParagraphNode(AnnotatedString("Second item")))),
                ListItemNode(persistentListOf(ParagraphNode(AnnotatedString("Third item")))),
            )
        )
    )
)

@Composable
private fun TimestampView() {
    Text(text = "12:34", modifier = Modifier.background(Color.LightGray).padding(start = 12.dp))
}

@Preview(showBackground = true)
@Composable
internal fun ContentAvoidingLayoutPreview() {
    Box(modifier = Modifier.padding(WindowInsets.safeContent.asPaddingValues())) {
        ContentAvoidingLayout(
            overlay = { TimestampView() },
            content = {
                HtmlMessageContent(
                    modifier = Modifier.background(Color.Red),
                    node = documentNode,
                    onContentLayoutChange = this::onContentLayoutChange,
                )
            }
        )
    }
}
