/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media.contentvalidation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.item.event.CallNotifyContent
import io.element.android.libraries.matrix.api.timeline.item.event.EventContent
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseMessageLikeContent
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseStateContent
import io.element.android.libraries.matrix.api.timeline.item.event.LegacyCallInviteContent
import io.element.android.libraries.matrix.api.timeline.item.event.LiveLocationContent
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileChangeContent
import io.element.android.libraries.matrix.api.timeline.item.event.RedactedContent
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import io.element.android.libraries.matrix.api.timeline.item.event.StateContent
import io.element.android.libraries.matrix.api.timeline.item.event.StickerContent
import io.element.android.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import io.element.android.libraries.matrix.api.timeline.item.event.UnknownContent
import io.element.android.libraries.matrix.api.timeline.item.event.isMediaContent

private val noopValue = NoopContentValidationState()

/**
 * A composition local that provides an [EventContentValidationCache] instance, so we can check if a given event content has been validated or not.
 */
val LocalEventContentValidationState = staticCompositionLocalOf<EventContentValidationCache> { NoopEventContentValidationCache() }

/**
 * A noop implementation of the [EventContentValidationCache] that immediately returns a successful validation state. This will be used in FOSS.
 */
class NoopEventContentValidationCache(
    private val provided: ContentValidationState = noopValue,
) : EventContentValidationCache {
    override operator fun get(eventId: EventId): ContentValidationState = provided
}

/**
 * A helper to remember the validation state of an event content, based on its [EventId] and [EventContent], if known.
 */
@Composable
fun rememberEventContentValidationState(eventId: EventId?, eventContent: EventContent?): ContentValidationState {
    val needsValidation = remember(eventContent) {
        when (eventContent) {
            is MessageContent -> eventContent.isMediaContent()
            is StickerContent -> true
            // If the event content is not known or not a media content, we don't need to validate it.
            null, is RedactedContent, is PollContent, is UnableToDecryptContent, is RoomMembershipContent, is ProfileChangeContent, is StateContent,
            is FailedToParseMessageLikeContent, is FailedToParseStateContent, is LiveLocationContent, LegacyCallInviteContent, is CallNotifyContent,
            UnknownContent -> false
        }
    }

    return rememberEventContentValidationState(eventId, needsValidation)
}

/**
 * A helper to remember the validation state of an event content, based on its [EventId] and whether it needs validation or not.
 */
@Composable
fun rememberEventContentValidationState(eventId: EventId?, needsValidation: Boolean): ContentValidationState {
    if (!needsValidation) {
        return noopValue
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
            noopValue
        }
    }
}
