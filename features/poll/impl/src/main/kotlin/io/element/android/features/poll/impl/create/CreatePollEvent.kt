/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.create

import io.element.android.libraries.matrix.api.poll.PollKind

sealed interface CreatePollEvent {
    data object Save : CreatePollEvent
    data class Delete(val confirmed: Boolean) : CreatePollEvent
    data class SetQuestion(val question: String) : CreatePollEvent
    data class SetAnswer(val index: Int, val text: String) : CreatePollEvent
    data object AddAnswer : CreatePollEvent
    data class RemoveAnswer(val index: Int) : CreatePollEvent
    data class SetPollKind(val pollKind: PollKind) : CreatePollEvent
    data object NavBack : CreatePollEvent
    data object ConfirmNavBack : CreatePollEvent
    data object HideConfirmation : CreatePollEvent
}
