/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media.contentvalidation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.item.event.EventContent
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.StickerContent
import io.element.android.libraries.matrix.api.timeline.item.event.isMediaContent

val LocalEventContentValidationState = staticCompositionLocalOf<EventContentValidationCache> { NoopEventContentValidationCache() }

class NoopEventContentValidationCache : EventContentValidationCache {
    override operator fun get(eventId: EventId): ContentValidationState {
        return ContentValidationState(mutableStateOf(AsyncData.Success(true)))
    }

    override fun evict(eventId: EventId) = Unit
}

@Composable
fun rememberEventContentValidationState(eventId: EventId?, eventContent: EventContent?): ContentValidationState {
    val needsValidation = remember(eventContent) {
        when (eventContent) {
            is MessageContent -> eventContent.isMediaContent()
            is StickerContent -> true
            else -> false
        }
    }

    return rememberEventContentValidationState(eventId, needsValidation)
}

@Composable
fun rememberEventContentValidationState(eventId: EventId?, needsValidation: Boolean): ContentValidationState {
    if (!needsValidation) {
        return ContentValidationState(isValid = true)
    }

    return rememberEventContentValidationStateInternal(eventId)
}

@Composable
private fun rememberEventContentValidationStateInternal(eventId: EventId?): ContentValidationState {
    val cache = LocalEventContentValidationState.current
    return remember(eventId) {
        if (eventId != null) {
            cache[eventId]
        } else {
            ContentValidationState()
        }
    }
}
