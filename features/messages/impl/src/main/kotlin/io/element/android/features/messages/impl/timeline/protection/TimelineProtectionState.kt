/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.protection

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.EventId
import kotlinx.collections.immutable.ImmutableSet

data class TimelineProtectionState(
    val protectionState: ProtectionState,
    val eventSink: (TimelineProtectionEvent) -> Unit,
) {
    fun hideMediaContent(eventId: EventId?) = when (protectionState) {
        is ProtectionState.RenderAll -> false
        is ProtectionState.RenderOnly -> eventId !in protectionState.eventIds
    }
}

@Immutable
sealed interface ProtectionState {
    data object RenderAll : ProtectionState
    data class RenderOnly(val eventIds: ImmutableSet<EventId>) : ProtectionState
}
