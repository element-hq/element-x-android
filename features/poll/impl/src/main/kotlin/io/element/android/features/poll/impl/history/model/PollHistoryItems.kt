/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
