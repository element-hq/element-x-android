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
import io.element.android.libraries.matrix.ui.messages.toHtmlDocument
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ToHtmlDocumentTest {

    @Test
    fun `toHtmlDocument - returns null if format is not HTML`() {
        val body = FormattedBody(
            format = MessageFormat.UNKNOWN,
            body = "Hello world"
        )

        val document = body.toHtmlDocument()

        assertThat(document).isNull()
    }

    @Test
    fun `toHtmlDocument - returns a Document if the format is HTML`() {
        val body = FormattedBody(
            format = MessageFormat.HTML,
            body = "<p>Hello world</p>"
        )

        val document = body.toHtmlDocument()
        assertThat(document).isNotNull()
        assertThat(document?.text()).isEqualTo("Hello world")
    }

    @Test
    fun `toHtmlDocument - returns a Document with a prefix if provided`() {
        val body = FormattedBody(
            format = MessageFormat.HTML,
            body = "<p>Hello world</p>"
        )

        val document = body.toHtmlDocument(prefix = "@Jorge:")
        assertThat(document).isNotNull()
        assertThat(document?.text()).isEqualTo("@Jorge: Hello world")
    }

    @Test
    fun `toHtmlDocument - if a mention is found without an '@' prefix, it will be added`() {
        val body = FormattedBody(
            format = MessageFormat.HTML,
            body = "Hey <a href='https://matrix.to/#/@alice:matrix.org'>Alice</a>!"
        )

        val document = body.toHtmlDocument()
        assertThat(document?.text()).isEqualTo("Hey @Alice!")
    }

    @Test
    fun `toHtmlDocument - if a mention is found with an '@' prefix, nothing will be done`() {
        val body = FormattedBody(
            format = MessageFormat.HTML,
            body = "Hey <a href='https://matrix.to/#/@alice:matrix.org'>@Alice</a>!"
        )

        val document = body.toHtmlDocument()
        assertThat(document?.text()).isEqualTo("Hey @Alice!")
    }

    @Test
    fun `toHtmlDocument - if a link is not a mention, nothing will be done for it`() {
        val body = FormattedBody(
            format = MessageFormat.HTML,
            body = "Hey <a href='https://matrix.org'>Alice</a>!"
        )

        val document = body.toHtmlDocument()
        assertThat(document?.text()).isEqualTo("Hey Alice!")
    }
}
