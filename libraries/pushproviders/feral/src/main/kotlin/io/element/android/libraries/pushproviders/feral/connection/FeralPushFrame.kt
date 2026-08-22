/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.feral.connection

import dev.zacsweers.metro.Inject
import io.element.android.libraries.androidutils.json.JsonProvider
import io.element.android.libraries.core.data.tryOrNull
import kotlinx.serialization.Serializable
import kotlin.io.encoding.Base64

/**
 * One frame of the ntfy WebSocket stream (`wss://server/<topic>/ws`), one JSON object per frame.
 * Unknown keys (title, tags, priority, attachment…) are ignored.
 */
@Serializable
data class FeralPushFrame(
    val id: String = "",
    val time: Long = 0,
    val event: String = "",
    val topic: String = "",
    val message: String? = null,
    /** "base64" when the published body was binary; absent for text. */
    val encoding: String? = null,
) {
    val isMessage: Boolean get() = event == EVENT_MESSAGE

    /** The published body: the raw Matrix push-gateway notification JSON for our topics. */
    fun body(): ByteArray {
        val text = message.orEmpty()
        return if (encoding == ENCODING_BASE64) {
            tryOrNull { Base64.Default.decode(text) } ?: ByteArray(0)
        } else {
            text.encodeToByteArray()
        }
    }

    companion object {
        const val EVENT_OPEN = "open"
        const val EVENT_KEEPALIVE = "keepalive"
        const val EVENT_MESSAGE = "message"
        const val ENCODING_BASE64 = "base64"
    }
}

@Inject
class FeralPushFrameParser(
    private val json: JsonProvider,
) {
    fun parse(text: String): FeralPushFrame? {
        return tryOrNull { json().decodeFromString<FeralPushFrame>(text) }
    }
}
