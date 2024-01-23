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

package io.element.android.libraries.matrixui.messages

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.timeline.item.event.FormattedBody
import io.element.android.libraries.matrix.api.timeline.item.event.MessageFormat
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.ui.messages.toPlainText
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
        assertThat(formattedBody.toPlainText()).isEqualTo(
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
        assertThat(formattedBody.toPlainText()).isNull()
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
        assertThat(messageType.toPlainText()).isEqualTo(
            """
            Hello world 
            • This is an unordered list.
            1. This is an ordered list.
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
        assertThat(messageType.toPlainText()).isEqualTo("This is the fallback text")
    }
}
