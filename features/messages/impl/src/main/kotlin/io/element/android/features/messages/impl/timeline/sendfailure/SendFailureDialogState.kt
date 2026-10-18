/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.sendfailure

import androidx.compose.runtime.Immutable
import io.element.android.features.messages.impl.timeline.model.TimelineItem

@Immutable
sealed interface SendFailureDialogState {
    data object Hidden : SendFailureDialogState

    data class Show(
        val event: TimelineItem.Event,
        val sendFailureType: SendFailureType,
    ) : SendFailureDialogState

    @Immutable
    sealed interface SendFailureType {
        data class InvalidMimeType(val mimeType: String) : SendFailureType
        data object MissingMediaContent : SendFailureType
        data object SendingFromUnverifiedDevice : SendFailureType
        data class Error(val message: String) : SendFailureType
        data object Unknown : SendFailureType
    }
}
