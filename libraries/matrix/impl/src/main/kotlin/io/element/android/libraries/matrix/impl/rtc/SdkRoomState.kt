/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.rtc

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.impl.util.mxCallbackFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flowOn
import org.matrix.rtc.FfiEventIn
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.RoomStateEvent
import org.matrix.rustcomponents.sdk.RoomStateEventsListener
import uniffi.ruma_events.stateEventTypeFromString

/**
 * The room's current state events of [eventType], one per state key, as complete sets: the SDK calls
 * the listener at once with the current set, an empty one included, then again after every sync that
 * changes it.
 *
 * Only the state the sync asks for is stored locally; the call member type is in sliding sync's default
 * `required_state`. Ruma treats `m.call.member` and `org.matrix.msc3401.call.member` as one type, so each
 * spelling gets the events of both, as from the core's own SDK backend.
 */
internal fun Room.stateEventSets(eventType: String, dispatchers: CoroutineDispatchers): Flow<List<FfiEventIn>> = mxCallbackFlow {
    subscribeToStateEvents(
        stateEventTypeFromString(eventType),
        object : RoomStateEventsListener {
            override fun onUpdate(events: List<RoomStateEvent>) {
                trySend(events.mapNotNull { it.toFfiEventIn(eventType) })
            }
        },
    )
}
    .buffer(Channel.CONFLATED)
    .flowOn(dispatchers.io)

/**
 * The subscribed type rather than the event's own: the SDK hands it back as a `StateEventType` whose
 * custom variant has no readable string. Stripped state (an invited room) has no event id and is dropped.
 */
internal fun RoomStateEvent.toFfiEventIn(eventType: String): FfiEventIn? {
    return FfiEventIn(
        eventId = eventId ?: return null,
        sender = sender,
        eventType = eventType,
        stateKey = stateKey,
        originServerTs = timestamp ?: return null,
        contentJson = contentJson,
        encryption = ClearText,
    )
}
