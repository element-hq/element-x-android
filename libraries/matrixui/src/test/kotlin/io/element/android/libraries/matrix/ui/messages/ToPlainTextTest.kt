/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.messages

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.timeline.item.event.FormattedBody
import io.element.android.libraries.matrix.api.timeline.item.event.MessageFormat
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import org.jsoup.Jsoup
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ToPlainTextTest {
    @Test
    fun `Document toPlainText - returns a plain text version of the document`() {
        val document = Jsoup.parse(
            """
            Hello world
            <ul><li>This is an unordered list.</li></ul>
            <ol><li>This is an ordered list.</li></ol>
            <br />
            """.trimIndent()
        )

        assertThat(document.toPlainText()).isEqualTo(
            """
            Hello world 
            • This is an unordered list.
            1. This is an ordered list.
            """.trimIndent()
        )
    }

    @Test
    fun `FormattedBody toPlainText - returns a plain text version of the HTML body`() {
        val formattedBody = FormattedBody(
            format = MessageFormat.HTML,
            body = """
                Hello world
                <ul><li>This is an unordered list.</li></ul>
                <ol><li>This is an ordered list.</li></ol>
                <br />
            """.trimIndent()
        )
        assertThat(formattedBody.toPlainText(permalinkParser = FakePermalinkParser())).isEqualTo(
            """
            Hello world 
            • This is an unordered list.
            1. This is an ordered list.
            """.trimIndent()
        )
    }

    @Test
    fun `FormattedBody toPlainText - returns null if the format is not HTML`() {
        val formattedBody = FormattedBody(
            format = MessageFormat.UNKNOWN,
            body = """
                Hello world
                <ul><li>This is an unordered list.</li></ul>
                <ol><li>This is an ordered list.</li></ol>
                <br />
            """.trimIndent()
        )
        assertThat(formattedBody.toPlainText(permalinkParser = FakePermalinkParser())).isNull()
    }

    @Test
    fun `TextMessageType toPlainText - returns a plain text version of the HTML body`() {
        val messageType = TextMessageType(
            body = "Hello world\n- This in an unordered list.\n1. This is an ordered list.\n",
            formatted = FormattedBody(
                format = MessageFormat.HTML,
                body = """
                    Hello world
                    <ul><li>This is an unordered list.</li></ul>
                    <ol><li>This is an ordered list.</li></ol>
                    <br />
                """.trimIndent()
            )
        )
        assertThat(messageType.toPlainText(permalinkParser = FakePermalinkParser())).isEqualTo(
            """
            Hello world 
            • This is an unordered list.
            1. This is an ordered list.
            """.trimIndent()
        )
    }

    @Test
    fun `TextMessageType toPlainText - respects the ol start attr if present`() {
        val messageType = TextMessageType(
            body = "1. First item\n2. Second item\n",
            formatted = FormattedBody(
                format = MessageFormat.HTML,
                body = """
                    <ol start='11'>
                        <li>First item.</li>
                        <li>Second item.</li>  
                    </ol>
                    <br />
                """.trimIndent()
            )
        )
        assertThat(messageType.toPlainText(permalinkParser = FakePermalinkParser())).isEqualTo(
            """
            11. First item.
            12. Second item.
            """.trimIndent()
        )
    }

    @Test
    fun `TextMessageType toPlainText - returns the markdown body if the formatted one cannot be parsed`() {
        val messageType = TextMessageType(
            body = "This is the fallback text",
            formatted = FormattedBody(
                format = MessageFormat.UNKNOWN,
                body = """
                    Hello world
                    <ul><li>This is an unordered list.</li></ul>
                    <ol><li>This is an ordered list.</li></ol>
                    <br />
                """.trimIndent()
            )
        )
        assertThat(messageType.toPlainText(permalinkParser = FakePermalinkParser())).isEqualTo("This is the fallback text")
    }
}
