/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.poll.impl.history.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class PollHistoryItems(
    val ongoing: ImmutableList<PollHistoryItem> = persistentListOf(),
    val past: ImmutableList<PollHistoryItem> = persistentListOf(),
) {
    val size = ongoing.size + past.size
}
