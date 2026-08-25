/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.contentscanner.impl

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.scanner.ContentScanner
import io.element.android.libraries.matrix.ui.media.contentvalidation.ContentValidationState
import io.element.android.libraries.matrix.ui.media.contentvalidation.DefaultContentValidationState
import io.element.android.libraries.matrix.ui.media.contentvalidation.EventContentValidationCache
import io.element.android.libraries.matrix.ui.media.contentvalidation.NoopContentValidationState
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

@SingleIn(RoomScope::class)
@ContributesBinding(RoomScope::class)
class DefaultEventContentValidationCache(
    private val contentScanner: ContentScanner?,
) : EventContentValidationCache {
    private val cache = ConcurrentHashMap<EventId, ContentValidationState>()

    override operator fun get(eventId: EventId): ContentValidationState {
        return cache.getOrPut(eventId) {
            if (contentScanner != null) {
                DefaultContentValidationState()
            } else {
                Timber.v("Content scanner is not available, returning NoopContentValidationState for eventId: $eventId")
                NoopContentValidationState()
            }
        }
    }
}
