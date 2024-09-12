/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.poll.impl.history.model

import io.element.android.features.poll.api.pollcontent.PollContentState

data class PollHistoryItem(
    val formattedDate: String,
    val state: PollContentState,
)
