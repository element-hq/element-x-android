/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.rtc

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.impl.util.mxCallbackFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.matrix.rtc.FfiEventIn
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.RoomMessageLikeEvent
import org.matrix.rustcomponents.sdk.RoomMessageLikeEventsListener
import uniffi.ruma_events.messageLikeEventTypeFromString

internal const val REDACTION_EVENT_TYPE = "m.room.redaction"

/**
 * The message-like events of [eventType] as syncs bring them, decrypted. Unbounded: a reaction dropped
 * is a raised hand nobody sees.
 */
internal fun Room.messageLikeEvents(eventType: String, dispatchers: CoroutineDispatchers): Flow<FfiEventIn> = mxCallbackFlow {
    subscribeToMessageLikeEvents(
        messageLikeEventTypeFromString(eventType),
        object : RoomMessageLikeEventsListener {
            override fun onEvent(event: RoomMessageLikeEvent) {
                trySend(event.toFfiEventIn(eventType))
            }
        },
    )
}
    .buffer(Channel.UNLIMITED)
    .flowOn(dispatchers.io)

/** The id of every event redacted from now on. */
internal fun Room.redactedEventIds(dispatchers: CoroutineDispatchers): Flow<String> =
    messageLikeEvents(REDACTION_EVENT_TYPE, dispatchers).mapNotNull { it.redactedEventId() }

/** As for state, the subscribed type: the SDK's `MessageLikeEventType` has no readable custom string. */
internal fun RoomMessageLikeEvent.toFfiEventIn(eventType: String) = FfiEventIn(
    eventId = eventId,
    sender = sender,
    eventType = eventType,
    stateKey = null,
    originServerTs = timestamp,
    contentJson = contentJson,
    encryption = encryptionInfo.toFfi(),
)

private val json = Json { ignoreUnknownKeys = true }

/** Room version 11 moved `redacts` into the content, and the SDK hands over the content only. */
internal fun FfiEventIn.redactedEventId(): String? {
    val content = runCatchingExceptions { json.parseToJsonElement(contentJson) as? JsonObject }.getOrNull()
    return (content?.get("redacts") as? JsonPrimitive)?.contentOrNull
}
