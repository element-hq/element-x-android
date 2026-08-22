/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.feral.connection

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.androidutils.json.DefaultJsonProvider
import org.junit.Test
import kotlin.io.encoding.Base64

class FeralPushFrameParserTest {
    private val parser = FeralPushFrameParser(DefaultJsonProvider())

    @Test
    fun `parses an open frame`() {
        val frame = parser.parse("""{"id":"abc","time":1700000000,"event":"open","topic":"upabc"}""")
        assertThat(frame).isEqualTo(FeralPushFrame(id = "abc", time = 1700000000, event = "open", topic = "upabc"))
        assertThat(frame!!.isMessage).isFalse()
    }

    @Test
    fun `parses a keepalive frame`() {
        val frame = parser.parse("""{"id":"k1","time":1,"event":"keepalive","topic":"upabc"}""")
        assertThat(frame!!.event).isEqualTo(FeralPushFrame.EVENT_KEEPALIVE)
        assertThat(frame.isMessage).isFalse()
        assertThat(frame.body()).isEmpty()
    }

    @Test
    fun `parses a text message frame and ignores unknown keys`() {
        val body = """{"notification":{"event_id":"${'$'}ev","room_id":"!room:server","counts":{"unread":2}}}"""
        val json = """{"id":"m1","time":2,"event":"message","topic":"upabc","message":${'"'}${body.replace("\"", "\\\"")}${'"'},"title":"t","priority":3,"tags":["a"]}"""
        val frame = parser.parse(json)
        assertThat(frame!!.isMessage).isTrue()
        assertThat(frame.id).isEqualTo("m1")
        assertThat(frame.encoding).isNull()
        assertThat(String(frame.body())).isEqualTo(body)
    }

    @Test
    fun `decodes a base64 message frame`() {
        val body = """{"notification":{"event_id":"${'$'}ev","room_id":"!room:server"}}"""
        val encoded = Base64.Default.encode(body.encodeToByteArray())
        val frame = parser.parse("""{"id":"m2","time":3,"event":"message","topic":"upabc","message":"$encoded","encoding":"base64"}""")
        assertThat(frame!!.isMessage).isTrue()
        assertThat(String(frame.body())).isEqualTo(body)
    }

    @Test
    fun `invalid base64 yields an empty body`() {
        val frame = parser.parse("""{"id":"m3","event":"message","message":"***","encoding":"base64"}""")
        assertThat(frame!!.body()).isEmpty()
    }

    @Test
    fun `returns null on garbage`() {
        assertThat(parser.parse("not json")).isNull()
        assertThat(parser.parse("")).isNull()
    }
}
