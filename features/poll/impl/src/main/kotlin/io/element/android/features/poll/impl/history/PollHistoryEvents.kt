/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.poll.impl.history

import io.element.android.features.poll.impl.history.model.PollHistoryFilter
import io.element.android.libraries.matrix.api.core.EventId

sealed interface PollHistoryEvents {
    data object LoadMore : PollHistoryEvents
    data class SelectPollAnswer(val pollStartId: EventId, val answerId: String) : PollHistoryEvents
    data class EndPoll(val pollStartId: EventId) : PollHistoryEvents
    data class SelectFilter(val filter: PollHistoryFilter) : PollHistoryEvents
}
