/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.poll.api.create

import io.element.android.libraries.matrix.api.core.EventId

sealed interface CreatePollMode {
    data object NewPoll : CreatePollMode
    data class EditPoll(val eventId: EventId) : CreatePollMode
}
