/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.api.pollcontent

import io.element.android.libraries.matrix.api.poll.PollAnswer

/**
 * UI model for a [PollAnswer].
 *
 * @property answer the poll answer.
 * @property isSelected whether the user has selected this answer.
 * @property isEnabled whether the answer can be voted.
 * @property isWinner whether this is the winner answer in the poll.
 * @property showVotes whether the votes for this answer should be displayed.
 * @property votesCount the number of votes for this answer.
 * @property percentage the percentage of votes for this answer.
 */
data class PollAnswerItem(
    val answer: PollAnswer,
    val isSelected: Boolean,
    val isEnabled: Boolean,
    val isWinner: Boolean,
    val showVotes: Boolean,
    val votesCount: Int,
    val percentage: Float,
)
