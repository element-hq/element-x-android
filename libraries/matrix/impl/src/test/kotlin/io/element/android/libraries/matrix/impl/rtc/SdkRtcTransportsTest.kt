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
            RtcTransport.LiveKit(serviceUrl = "https://sfu.example.org/jwt"),
            RtcTransport.Custom(transportType = "org.example.mesh", data = """{"url":"https://mesh.example.org"}"""),
        ).toJson()

        assertThat(json).isEqualTo(
            """[{"type":"livekit","livekit_service_url":"https://sfu.example.org/jwt"},""" +
                """{"url":"https://mesh.example.org","type":"org.example.mesh"}]"""
        )
    }

    @Test
    fun `no transport is an empty array`() {
        assertThat(emptyList<RtcTransport>().toJson()).isEqualTo("[]")
    }
}
