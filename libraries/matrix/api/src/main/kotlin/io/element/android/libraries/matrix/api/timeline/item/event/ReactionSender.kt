/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.timeline.item.event

import io.element.android.libraries.matrix.api.core.UserId

/**
 * The sender of a reaction.
 *
 * @property senderId the ID of the user who sent the reaction
 * @property timestamp the timestamp the reaction was received on the origin homeserver
 */
data class ReactionSender(
    val senderId: UserId,
    val timestamp: Long
)
