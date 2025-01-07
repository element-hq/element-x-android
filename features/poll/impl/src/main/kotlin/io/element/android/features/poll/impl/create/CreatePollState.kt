/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.create

import io.element.android.libraries.matrix.api.poll.PollKind
import kotlinx.collections.immutable.ImmutableList

data class CreatePollState(
    val mode: Mode,
    val canSave: Boolean,
    val canAddAnswer: Boolean,
    val question: String,
    val answers: ImmutableList<Answer>,
    val pollKind: PollKind,
    val showBackConfirmation: Boolean,
    val showDeleteConfirmation: Boolean,
    val eventSink: (CreatePollEvents) -> Unit,
) {
    enum class Mode {
        New,
        Edit,
    }

    val canDelete: Boolean = mode == Mode.Edit
}

data class Answer(
    val text: String,
    val canDelete: Boolean,
)
