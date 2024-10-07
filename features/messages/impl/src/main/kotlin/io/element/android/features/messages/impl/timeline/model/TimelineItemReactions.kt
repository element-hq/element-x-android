/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

data class TimelineItemReactions(
    val reactions: ImmutableList<AggregatedReaction>
) {
    val highlightedKeys: ImmutableList<String>
        get() = reactions
            .filter { it.isHighlighted }
            .map { it.key }
            .toPersistentList()
}
