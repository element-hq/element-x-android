/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.rtc

import io.element.android.libraries.core.extensions.runCatchingExceptions
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.matrix.rustcomponents.sdk.RtcTransport

private val json = Json { ignoreUnknownKeys = true }

/** The `rtc_transports` array the core parses, rebuilt from the SDK's typed transports. */
internal fun List<RtcTransport>.toJson(): String = JsonArray(map { it.toJsonObject() }).toString()

private fun RtcTransport.toJsonObject(): JsonObject = when (this) {
    is RtcTransport.LiveKit -> JsonObject(mapOf("type" to JsonPrimitive("livekit"), "livekit_service_url" to JsonPrimitive(serviceUrl)))
    is RtcTransport.Custom -> {
        val fields = runCatchingExceptions { json.parseToJsonElement(data) as? JsonObject }.getOrNull().orEmpty()
        JsonObject(fields + ("type" to JsonPrimitive(transportType)))
    }
}
