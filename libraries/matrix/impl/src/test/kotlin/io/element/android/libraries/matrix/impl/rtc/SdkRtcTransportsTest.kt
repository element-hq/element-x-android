/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.rtc

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.matrix.rustcomponents.sdk.RtcTransport

class SdkRtcTransportsTest {
    @Test
    fun `transports keep their order, as the core picks the first it can use`() {
        val json = listOf(
            RtcTransport.LegacyLiveKit(url = null, serviceUrl = "https://sfu.example.org/jwt"),
            RtcTransport.Custom(transportType = "org.example.mesh", data = """{"url":"https://mesh.example.org"}"""),
        ).toJson()

        assertThat(json).isEqualTo(
            """[{"type":"livekit","livekit_service_url":"https://sfu.example.org/jwt"},""" +
                """{"url":"https://mesh.example.org","type":"org.example.mesh"}]"""
        )
    }

    @Test
    fun `m livekit transport keeps its url, and its service url when present`() {
        val json = listOf(
            RtcTransport.LiveKit(url = "wss://sfu.example.org", serviceUrl = null),
            RtcTransport.LiveKit(url = "wss://sfu2.example.org", serviceUrl = "https://sfu2.example.org/jwt"),
        ).toJson()

        assertThat(json).isEqualTo(
            """[{"type":"m.livekit","url":"wss://sfu.example.org"},""" +
                """{"type":"m.livekit","url":"wss://sfu2.example.org","livekit_service_url":"https://sfu2.example.org/jwt"}]"""
        )
    }

    @Test
    fun `legacy livekit transport keeps the urls that are present`() {
        val json = listOf(
            RtcTransport.LegacyLiveKit(url = "wss://sfu.example.org", serviceUrl = "https://sfu.example.org/jwt"),
            RtcTransport.LegacyLiveKit(url = "wss://sfu2.example.org", serviceUrl = null),
        ).toJson()

        assertThat(json).isEqualTo(
            """[{"type":"livekit","url":"wss://sfu.example.org","livekit_service_url":"https://sfu.example.org/jwt"},""" +
                """{"type":"livekit","url":"wss://sfu2.example.org"}]"""
        )
    }

    @Test
    fun `a homeserver advertising the same sfu as livekit and m livekit gives each transport once`() {
        val json = listOf(
            RtcTransport.LegacyLiveKit(url = "wss://sfu.example.org", serviceUrl = "https://sfu.example.org"),
            RtcTransport.LiveKit(url = "wss://sfu.example.org", serviceUrl = null),
        ).toJson()

        assertThat(json).isEqualTo(
            """[{"type":"livekit","url":"wss://sfu.example.org","livekit_service_url":"https://sfu.example.org"},""" +
                """{"type":"m.livekit","url":"wss://sfu.example.org"}]"""
        )
    }

    @Test
    fun `no transport is an empty array`() {
        assertThat(emptyList<RtcTransport>().toJson()).isEqualTo("[]")
    }
}
