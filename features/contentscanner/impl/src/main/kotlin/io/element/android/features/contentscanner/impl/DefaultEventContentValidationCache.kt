/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.contentscanner.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.features.contentscanner.api.ContentValidationState
import io.element.android.features.contentscanner.api.EventContentValidationCache
import io.element.android.libraries.matrix.api.core.EventId
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.getOrPut

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultEventContentValidationCache : EventContentValidationCache {
    private val cache = ConcurrentHashMap<EventId, ContentValidationState>()

    override operator fun get(eventId: EventId): ContentValidationState {
        return cache.getOrPut(eventId) { ContentValidationState() }
    }

    override fun evict(eventId: EventId) {
        cache.remove(eventId)
    }
}
