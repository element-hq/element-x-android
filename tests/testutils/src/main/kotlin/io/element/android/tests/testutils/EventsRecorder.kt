/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.testutils

import com.google.common.truth.Truth.assertThat

class EventsRecorder<T>(
    private val expectEvents: Boolean = true
) : (T) -> Unit {
    private val events = mutableListOf<T>()

    override fun invoke(event: T) {
        if (expectEvents) {
            events.add(event)
        } else {
            throw AssertionError("Unexpected event: $event")
        }
    }

    fun assertEmpty() {
        assertThat(events).isEmpty()
    }

    fun assertSingle(event: T) {
        assertList(listOf(event))
    }

    fun assertList(expectedEvents: List<T>) {
        assertThat(events).isEqualTo(expectedEvents)
    }

    fun assertSize(size: Int) {
        assertThat(events.size).isEqualTo(size)
    }

    fun assertTrue(index: Int, predicate: (T) -> Boolean) {
        assertThat(predicate(events[index])).isTrue()
    }

    fun clear() {
        events.clear()
    }
}
