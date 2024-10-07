/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.poll.impl.history.model

import io.element.android.features.poll.impl.R

enum class PollHistoryFilter(val stringResource: Int) {
    ONGOING(R.string.screen_polls_history_filter_ongoing),
    PAST(R.string.screen_polls_history_filter_past),
}
