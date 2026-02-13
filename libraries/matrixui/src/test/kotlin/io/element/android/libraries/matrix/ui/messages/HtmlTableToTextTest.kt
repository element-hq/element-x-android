/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.messages

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.timeline.item.event.FormattedBody
import io.element.android.libraries.matrix.api.timeline.item.event.MessageFormat
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import org.jsoup.Jsoup
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HtmlTableToTextTest {
    @Test
    fun `simple 2x2 table without header`() {
        val doc = Jsoup.parse(
            "<table><tr><td>A</td><td>B</td></tr><tr><td>C</td><td>D</td></tr></table>"
        )
        doc.convertTablesToText()

        val code = doc.selectFirst("pre > code")
        assertThat(code).isNotNull()
        assertThat(code!!.wholeText()).isEqualTo(
            "A | B\n" +
                "C | D"
        )
    }

    @Test
    fun `table with thead header`() {
        val doc = Jsoup.parse(
            "<table>" +
                "<thead><tr><th>Header A</th><th>Header B</th></tr></thead>" +
                "<tbody><tr><td>Cell 1</td><td>Cell 2</td></tr>" +
                "<tr><td>Cell 3</td><td>Cell 4</td></tr></tbody>" +
                "</table>"
        )
        doc.convertTablesToText()

        val code = doc.selectFirst("pre > code")
        assertThat(code).isNotNull()
        assertThat(code!!.wholeText()).isEqualTo(
            "Header A | Header B\n" +
                "---------+---------\n" +
                "Cell 1   | Cell 2  \n" +
                "Cell 3   | Cell 4  "
        )
    }

    @Test
    fun `table with th in first row and no thead`() {
        val doc = Jsoup.parse(
            "<table>" +
                "<tr><th>Name</th><th>Age</th></tr>" +
                "<tr><td>Alice</td><td>30</td></tr>" +
                "</table>"
        )
        doc.convertTablesToText()

        val code = doc.selectFirst("pre > code")
        assertThat(code).isNotNull()
        assertThat(code!!.wholeText()).isEqualTo(
            "Name  | Age\n" +
                "------+----\n" +
                "Alice | 30 "
        )
    }

    @Test
    fun `unequal column counts - shorter rows padded`() {
        val doc = Jsoup.parse(
            "<table>" +
                "<tr><td>A</td><td>B</td><td>C</td></tr>" +
                "<tr><td>D</td></tr>" +
                "</table>"
        )
        doc.convertTablesToText()

        val code = doc.selectFirst("pre > code")
        assertThat(code).isNotNull()
        assertThat(code!!.wholeText()).isEqualTo(
            "A | B | C\n" +
                "D |   |  "
        )
    }

    @Test
    fun `empty table - no rows`() {
        val doc = Jsoup.parse("<table></table>")
        doc.convertTablesToText()

        // Table should be removed, no pre/code created
        assertThat(doc.selectFirst("table")).isNull()
        assertThat(doc.selectFirst("pre")).isNull()
    }

    @Test
    fun `table with surrounding content`() {
        val doc = Jsoup.parse(
            "<p>Before</p><table><tr><td>X</td></tr></table><p>After</p>"
        )
        doc.convertTablesToText()

        assertThat(doc.selectFirst("table")).isNull()
        assertThat(doc.text()).contains("Before")
        assertThat(doc.text()).contains("After")
        assertThat(doc.selectFirst("pre > code")).isNotNull()
        assertThat(doc.selectFirst("pre > code")!!.text()).isEqualTo("X")
    }

    @Test
    fun `multiple tables converted independently`() {
        val doc = Jsoup.parse(
            "<table><tr><td>T1</td></tr></table>" +
                "<table><tr><td>T2</td></tr></table>"
        )
        doc.convertTablesToText()

        val codes = doc.select("pre > code")
        assertThat(codes).hasSize(2)
        assertThat(codes[0].text()).isEqualTo("T1")
        assertThat(codes[1].text()).isEqualTo("T2")
    }

    @Test
    fun `single column table - no pipes`() {
        val doc = Jsoup.parse(
            "<table><tr><td>Row 1</td></tr><tr><td>Row 2</td></tr></table>"
        )
        doc.convertTablesToText()

        val code = doc.selectFirst("pre > code")
        assertThat(code).isNotNull()
        assertThat(code!!.wholeText()).isEqualTo(
            "Row 1\n" +
                "Row 2"
        )
    }

    @Test
    fun `cell content with extra whitespace is trimmed`() {
        val doc = Jsoup.parse(
            "<table><tr><td>  hello  </td><td>  world  </td></tr></table>"
        )
        doc.convertTablesToText()

        val code = doc.selectFirst("pre > code")
        assertThat(code).isNotNull()
        assertThat(code!!.wholeText()).isEqualTo("hello | world")
    }

    @Test
    fun `integration via toHtmlDocument - table is replaced`() {
        val body = FormattedBody(
            format = MessageFormat.HTML,
            body = "<p>Info:</p><table><thead><tr><th>Key</th><th>Value</th></tr></thead>" +
                "<tbody><tr><td>A</td><td>1</td></tr></tbody></table>"
        )

        val document = body.toHtmlDocument(permalinkParser = FakePermalinkParser())
        assertThat(document).isNotNull()
        // Table should have been replaced
        assertThat(document!!.selectFirst("table")).isNull()
        val code = document.selectFirst("pre > code")
        assertThat(code).isNotNull()
        assertThat(code!!.wholeText()).isEqualTo(
            "Key | Value\n" +
                "----+------\n" +
                "A   | 1    "
        )
    }
}
