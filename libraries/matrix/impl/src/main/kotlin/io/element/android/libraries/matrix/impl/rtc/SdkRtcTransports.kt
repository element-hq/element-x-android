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

/**
 * The `rtc_transports` array the core parses, rebuilt from the SDK's typed transports.
 *
 * Temporary: the SDK folds `m.livekit` and the legacy `livekit` into one [RtcTransport.LiveKit] and drops which one the
 * homeserver sent, while the core picks the first transport of its membership format's type: `m.livekit` for MSC,
 * `livekit` for room state. So each LiveKit transport is given back as `livekit`, and also as `m.livekit` when it names
 * an SFU `url`, the only field `m.livekit` has. Without that, an MSC join finds no `m.livekit` transport on a homeserver
 * that advertises one. Once the SDK keeps the type, map it back one to one and drop the duplication.
 */
internal fun List<RtcTransport>.toJson(): String = JsonArray(flatMap { it.toJsonObjects() }.distinct()).toString()

private fun RtcTransport.toJsonObjects(): List<JsonObject> = when (this) {
    is RtcTransport.LiveKit -> listOfNotNull(
        JsonObject(
            buildMap {
                put("type", JsonPrimitive("livekit"))
                // Mirror the MSC4195 wire format: absent fields are omitted rather than serialized as null.
                url?.let { put("url", JsonPrimitive(it)) }
                serviceUrl?.let { put("livekit_service_url", JsonPrimitive(it)) }
            }
        ),
        url?.let { JsonObject(mapOf("type" to JsonPrimitive("m.livekit"), "url" to JsonPrimitive(it))) },
    )
    is RtcTransport.Custom -> {
        val fields = runCatchingExceptions { json.parseToJsonElement(data) as? JsonObject }.getOrNull().orEmpty()
        listOf(JsonObject(fields + ("type" to JsonPrimitive(transportType))))
    }
}
