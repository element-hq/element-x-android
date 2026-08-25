/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media.contentvalidation

import io.element.android.libraries.matrix.api.core.EventId

/**
 * In-memory variant of [EventContentValidationCache] that holds the validation state of event contents, based on their [EventId].
 *
 * Note: this is intended to be used in tests.
 */
class InMemoryEventContentValidationCache(
    initial: Map<EventId, ContentValidationState> = emptyMap(),
) : EventContentValidationCache {
    private val cache = initial.toMutableMap()

    override operator fun get(eventId: EventId): ContentValidationState {
        return cache[eventId] ?: DefaultContentValidationState()
    }

    fun put(eventId: EventId, state: ContentValidationState) {
        cache[eventId] = state
    }
}
