/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.impl

import android.net.Uri
import android.os.Parcel
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Typeface
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.htmlrenderer.api.HeaderNode
import io.element.android.libraries.htmlrenderer.api.ParagraphNode
import io.element.android.libraries.htmlrenderer.api.parcel.AnnotatedStringParceler
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.jsoup.Jsoup
import org.junit.Test

class AnnotatedStringParcelerTest : RobolectricTest() {
    @Test
    fun `parsed message text survives a parcel round trip`() {
        val permalinkParser = FakePermalinkParser { url ->
            when {
                url.contains("@alice") -> PermalinkData.UserLink(UserId("@alice:example.org"))
                url.contains("#room") -> PermalinkData.RoomLink(roomIdOrAlias = RoomAlias("#room:example.org").toRoomIdOrAlias())
                else -> PermalinkData.FallbackLink(Uri.parse(url))
            }
        }
        val html = """
            <h2>Title with <em>emphasis</em></h2>
            <p>
                <strong>Bold <em>and italic</em></strong>, <u>underline</u>, <del>deleted</del> and <code>inline code</code>.
                Visit <a href="https://element.io">the <b>site</b></a>, ping <a href="https://matrix.to/#/@alice:example.org">Alice</a>,
                join <a href="https://matrix.to/#/#room:example.org">the room</a> or notify @room.
            </p>
        """.trimIndent()
        val document = DefaultHtmlMessageParser(permalinkParser).parse(Jsoup.parse(html))
        val texts = document.children.mapNotNull {
            when (it) {
                is ParagraphNode -> it.text
                is HeaderNode -> it.text
                else -> null
            }
        }
        assertThat(texts).hasSize(2)

        texts.forEach { text ->
            assertThat(text.roundTrip()).isEqualTo(text)
        }
    }

    @Test
    fun `all parcelable span style fields survive a parcel round trip`() {
        val text = buildAnnotatedString {
            append("Hello world")
            addStyle(
                SpanStyle(
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Italic,
                    fontFamily = FontFamily.Serif,
                    fontFeatureSettings = "smcp",
                    letterSpacing = 0.1.em,
                    baselineShift = BaselineShift.Superscript,
                    textGeometricTransform = TextGeometricTransform(scaleX = 2f, skewX = 0.5f),
                    localeList = LocaleList("en-GB,fr-FR"),
                    background = Color.Blue,
                    textDecoration = TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough)),
                    shadow = Shadow(color = Color.Green, offset = Offset(1f, 2f), blurRadius = 3f),
                ),
                start = 0,
                end = 5,
            )
            addStyle(SpanStyle(fontFamily = FontFamily.Monospace), start = 6, end = 11)
            addStringAnnotation(tag = "tag", annotation = "value", start = 0, end = 5)
            addLink(LinkAnnotation.Url("https://element.io"), start = 6, end = 11)
        }

        assertThat(text.roundTrip()).isEqualTo(text)
    }

    @Test
    fun `non parcelable values are skipped without losing the rest`() {
        val text = buildAnnotatedString {
            append("Hello world")
            addStyle(
                SpanStyle(
                    brush = Brush.linearGradient(listOf(Color.Red, Color.Blue)),
                    fontFamily = FontFamily(Typeface(android.graphics.Typeface.DEFAULT)),
                    fontWeight = FontWeight.Bold,
                ),
                start = 0,
                end = 5,
            )
            addLink(LinkAnnotation.Clickable(tag = "clickable", linkInteractionListener = {}), start = 0, end = 5)
            addLink(LinkAnnotation.Url("https://element.io"), start = 6, end = 11)
        }

        val result = text.roundTrip()

        assertThat(result.text).isEqualTo("Hello world")
        val style = result.spanStyles.single()
        assertThat(style.item).isEqualTo(SpanStyle(fontWeight = FontWeight.Bold))
        assertThat(style.start).isEqualTo(0)
        assertThat(style.end).isEqualTo(5)
        val link = result.getLinkAnnotations(0, result.length).single()
        assertThat(link.item).isEqualTo(LinkAnnotation.Url("https://element.io"))
        assertThat(link.start).isEqualTo(6)
        assertThat(link.end).isEqualTo(11)
    }

    private fun AnnotatedString.roundTrip(): AnnotatedString {
        val parcel = Parcel.obtain()
        try {
            with(AnnotatedStringParceler) { write(parcel, 0) }
            parcel.setDataPosition(0)
            return AnnotatedStringParceler.create(parcel)
        } finally {
            parcel.recycle()
        }
    }
}
