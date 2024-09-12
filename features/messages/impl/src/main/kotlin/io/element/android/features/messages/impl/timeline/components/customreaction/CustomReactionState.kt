/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.customreaction

import io.element.android.emojibasebindings.EmojibaseStore
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import kotlinx.collections.immutable.ImmutableSet

data class CustomReactionState(
    val target: Target,
    val selectedEmoji: ImmutableSet<String>,
    val eventSink: (CustomReactionEvents) -> Unit,
) {
    sealed interface Target {
        data object None : Target
        data class Loading(val event: TimelineItem.Event) : Target
        data class Success(
            val event: TimelineItem.Event,
            val emojibaseStore: EmojibaseStore,
        ) : Target
    }
}
