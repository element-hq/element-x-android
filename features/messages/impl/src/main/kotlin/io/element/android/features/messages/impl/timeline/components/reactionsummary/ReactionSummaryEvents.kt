/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.reactionsummary

import io.element.android.features.messages.impl.timeline.model.AggregatedReaction
import io.element.android.libraries.matrix.api.core.EventId

sealed interface ReactionSummaryEvents {
    data object Clear : ReactionSummaryEvents
    data class ShowReactionSummary(val eventId: EventId, val reactions: List<AggregatedReaction>, val selectedKey: String) : ReactionSummaryEvents
}
