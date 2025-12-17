/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model

import io.element.android.libraries.core.extensions.ellipsize
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.collections.immutable.ImmutableList

/**
 * Length at which we ellipsize a reaction key for display
 *
 * Reactions can be free text, so we need to limit the length
 * displayed on screen.
 */
private const val MAX_DISPLAY_CHARS = 16

/**
 * @property currentUserId the ID of the currently logged in user
 * @property key the full reaction key (e.g. "👍", "YES!")
 * @property senders the list of users who sent the reactions
 */
data class AggregatedReaction(
    val currentUserId: UserId,
    val key: String,
    val senders: ImmutableList<AggregatedReactionSender>
) {
    /**
     * The key to be displayed on screen.
     *
     * See [MAX_DISPLAY_CHARS].
     */
    val displayKey: String = key.ellipsize(MAX_DISPLAY_CHARS)

    /**
     * The number of users who reacted with this key.
     */
    val count: Int = senders.count()

    /**
     * True if the reaction has (also) been sent by the current user.
     */
    val isHighlighted: Boolean = senders.any { it.senderId == currentUserId }
}
