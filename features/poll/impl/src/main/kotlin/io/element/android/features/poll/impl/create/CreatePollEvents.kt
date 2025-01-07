/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.create

import io.element.android.libraries.matrix.api.poll.PollKind

sealed interface CreatePollEvents {
    data object Save : CreatePollEvents
    data class Delete(val confirmed: Boolean) : CreatePollEvents
    data class SetQuestion(val question: String) : CreatePollEvents
    data class SetAnswer(val index: Int, val text: String) : CreatePollEvents
    data object AddAnswer : CreatePollEvents
    data class RemoveAnswer(val index: Int) : CreatePollEvents
    data class SetPollKind(val pollKind: PollKind) : CreatePollEvents
    data object NavBack : CreatePollEvents
    data object ConfirmNavBack : CreatePollEvents
    data object HideConfirmation : CreatePollEvents
}
