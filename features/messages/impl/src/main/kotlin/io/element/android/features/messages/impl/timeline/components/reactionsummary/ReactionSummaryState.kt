/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.reactionsummary

import io.element.android.features.messages.impl.timeline.model.AggregatedReaction
import io.element.android.libraries.matrix.api.core.EventId
import kotlinx.collections.immutable.ImmutableList

data class ReactionSummaryState(
    val target: Summary?,
    val eventSink: (ReactionSummaryEvents) -> Unit
) {
    data class Summary(
        val isDebugBuild: Boolean,
        val reactions: ImmutableList<AggregatedReaction>,
        val selectedKey: String,
        val selectedEventId: EventId
    )
}
