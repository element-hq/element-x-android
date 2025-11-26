/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.workmanager

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.androidutils.json.DefaultJsonProvider
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_ID_3
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.push.api.push.NotificationEventRequest
import org.junit.Test

class WorkerDataConverterTest {
    @Test
    fun `ensure identity when serializing - deserializing an empty list`() {
        testIdentity(emptyList())
    }

    @Test
    fun `ensure identity when serializing - deserializing a list`() {
        testIdentity(
            listOf(
                NotificationEventRequest(
                    sessionId = A_SESSION_ID,
                    roomId = A_ROOM_ID,
                    eventId = AN_EVENT_ID,
                    providerInfo = "info1",
                ),
                NotificationEventRequest(
                    sessionId = A_SESSION_ID_2,
                    roomId = A_ROOM_ID_2,
                    eventId = AN_EVENT_ID_2,
                    providerInfo = "info2",
                ),
            )
        )
    }

    @Test
    fun `serializing lots of data leads to several work data generated - one room - 100 events should be split in 5 chunks`() {
        val data = List(100) {
            NotificationEventRequest(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                eventId = EventId(AN_EVENT_ID.value + it),
                providerInfo = "info$it",
            )
        }
        val sut = WorkerDataConverter(DefaultJsonProvider())
        val serialized = sut.serialize(data)
        assertThat(serialized.getOrNull()?.size).isGreaterThan(1)
        assertThat(serialized.getOrNull()?.size).isEqualTo(100 / WorkerDataConverter.CHUNK_SIZE)
        // All the items are present
        val deserialized = serialized.getOrNull()?.flatMap { sut.deserialize(it)!! }
        assertThat(deserialized).containsExactlyElementsIn(data)
    }

    @Test
    fun `serializing lots of data leads to several work data generated - one room - 101 events should be split in 6 chunks`() {
        val data = List(101) {
            NotificationEventRequest(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                eventId = EventId(AN_EVENT_ID.value + it),
                providerInfo = "info$it",
            )
        }
        val sut = WorkerDataConverter(DefaultJsonProvider())
        val serialized = sut.serialize(data)
        assertThat(serialized.getOrNull()?.size).isGreaterThan(1)
        assertThat(serialized.getOrNull()?.size).isEqualTo(100 / WorkerDataConverter.CHUNK_SIZE + 1)
        // All the items are present
        val deserialized = serialized.getOrNull()?.flatMap { sut.deserialize(it)!! }
        assertThat(deserialized).containsExactlyElementsIn(data)
    }

    @Test
    fun `serializing lots of data leads to several work data generated - 3 rooms - 25 events should be split in 2 chunks and room not mixed`() {
        val data1 = List(15) {
            NotificationEventRequest(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                eventId = EventId(AN_EVENT_ID.value + it),
                providerInfo = "info".repeat(100) + it,
            )
        }
        val data2 = List(3) {
            NotificationEventRequest(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID_2,
                eventId = EventId(AN_EVENT_ID.value + it),
                providerInfo = "info".repeat(100) + it,
            )
        }
        val data3 = List(7) {
            NotificationEventRequest(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID_3,
                eventId = EventId(AN_EVENT_ID.value + it),
                providerInfo = "info".repeat(100) + it,
            )
        }
        val data = (data1 + data2 + data3).shuffled()
        val sut = WorkerDataConverter(DefaultJsonProvider())
        val serialized = sut.serialize(data)
        assertThat(serialized.getOrNull()?.size).isEqualTo(2)
        // All the items are present
        val deserialized = serialized.getOrNull()?.flatMap { sut.deserialize(it)!! }
        assertThat(deserialized).containsExactlyElementsIn(data)
        // Rooms are not mixed between the chunks
        val setsOfRooms = serialized.getOrNull()!!
            .map { workData -> sut.deserialize(workData)!! }
            .map {
                it.map { request -> request.roomId }.toSet()
            }
        // Ensure that all sets are distinct
        assertThat(setsOfRooms.size).isEqualTo(2)
        // 3 roomId are present
        assertThat(setsOfRooms.flatten().toSet()).containsExactly(A_ROOM_ID, A_ROOM_ID_2, A_ROOM_ID_3)
        // No intersection between sets
        assertThat(setsOfRooms[0].intersect(setsOfRooms[1])).isEmpty()
    }

    private fun testIdentity(data: List<NotificationEventRequest>) {
        val sut = WorkerDataConverter(DefaultJsonProvider())
        val serialized = sut.serialize(data).getOrThrow()
        val result = sut.deserialize(serialized.first())
        assertThat(result).isEqualTo(data)
    }
}
