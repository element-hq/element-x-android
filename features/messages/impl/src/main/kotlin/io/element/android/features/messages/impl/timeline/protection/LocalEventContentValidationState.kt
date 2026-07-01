/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.protection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import io.element.android.features.contentscanner.api.ContentValidationState
import io.element.android.features.contentscanner.api.EventContentValidationCache
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.EventId

val LocalEventContentValidationState = staticCompositionLocalOf<EventContentValidationCache> { NoopEventContentValidationCache() }

class NoopEventContentValidationCache : EventContentValidationCache {
    override operator fun get(eventId: EventId): ContentValidationState {
        return ContentValidationState(mutableStateOf(AsyncData.Success(true)))
    }

    override fun evict(eventId: EventId) = Unit
}

@Composable
fun rememberEventContentValidationState(eventId: EventId?): ContentValidationState {
    val cache = LocalEventContentValidationState.current
    return remember(eventId) {
        if (eventId != null) {
            cache[eventId]
        } else {
            ContentValidationState()
        }
    }
}
