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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.longOrNull
import org.matrix.rtc.FfiEventIn
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.StickyEvent
import org.matrix.rustcomponents.sdk.StickyEventsListener
import org.matrix.rustcomponents.sdk.StickyEventsUpdate
import org.matrix.rustcomponents.sdk.StickyKey
import timber.log.Timber

/** The room's live sticky events as complete sets, the first one as soon as it is collected. */
internal fun Room.stickyEventSets(dispatchers: CoroutineDispatchers): Flow<List<FfiEventIn>> = mxCallbackFlow {
    val set = StickyEventSet()
    subscribeToStickyEvents(
        object : StickyEventsListener {
            override fun onUpdate(update: StickyEventsUpdate) {
                trySend(set.apply(update))
            }
        }
    )
}.flowOn(dispatchers.io)

/** Folds the SDK's resets and changes into the whole set, which is what the core wants. */
internal class StickyEventSet {
    private val live = LinkedHashMap<StickyKey, FfiEventIn>()

    fun apply(update: StickyEventsUpdate): List<FfiEventIn> {
        when (update) {
            is StickyEventsUpdate.Reset -> {
                live.clear()
                update.events.forEach { put(it) }
            }
            is StickyEventsUpdate.Changes -> {
                update.added.forEach { put(it) }
                update.updated.forEach { put(it) }
                update.removed.forEach { live.remove(it.key) }
            }
        }
        return live.values.toList()
    }

    private fun put(event: StickyEvent) {
        val ffiEvent = event.toFfiEventIn()
        if (ffiEvent == null) {
            Timber.w("MatrixRTC backend: ignoring a malformed sticky event ${event.eventId}")
            live.remove(event.key)
        } else {
            live[event.key] = ffiEvent
        }
    }
}

private val json = Json { ignoreUnknownKeys = true }

internal fun StickyEvent.toFfiEventIn(): FfiEventIn? {
    val event = runCatchingExceptions { json.parseToJsonElement(eventJson) as? JsonObject }.getOrNull() ?: return null
    val content = event["content"] as? JsonObject ?: return null
    val timestampMs = (event["origin_server_ts"] as? JsonPrimitive)?.longOrNull ?: return null
    return FfiEventIn(
        eventId = eventId,
        sender = key.sender,
        // The type on the wire, which for an unstable type may differ from the key's.
        eventType = (event["type"] as? JsonPrimitive)?.contentOrNull ?: key.eventType,
        stateKey = null,
        originServerTs = timestampMs.toULong(),
        contentJson = json.encodeToString(JsonObject.serializer(), content),
        encryption = encryptionInfo.toFfi(),
    )
}
