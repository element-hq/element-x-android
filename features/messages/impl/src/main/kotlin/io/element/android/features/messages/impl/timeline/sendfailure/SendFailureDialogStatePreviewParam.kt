/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.sendfailure

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent

class SendFailureDialogStatePreviewParam : PreviewParameterProvider<SendFailureDialogState> {
    override val values: Sequence<SendFailureDialogState>
        get() = sequenceOf(
            SendFailureDialogState.Hidden,
            SendFailureDialogState.Show(
                event = aTimelineItemEvent(),
                sendFailureType = SendFailureDialogState.SendFailureType.InvalidMimeType("image/invalid")
            ),
            SendFailureDialogState.Show(
                event = aTimelineItemEvent(),
                sendFailureType = SendFailureDialogState.SendFailureType.MissingMediaContent,
            ),
            SendFailureDialogState.Show(
                event = aTimelineItemEvent(),
                sendFailureType = SendFailureDialogState.SendFailureType.SendingFromUnverifiedDevice,
            ),
            SendFailureDialogState.Show(
                event = aTimelineItemEvent(),
                sendFailureType = SendFailureDialogState.SendFailureType.Error("Message error from server"),
            ),
            SendFailureDialogState.Show(
                event = aTimelineItemEvent(),
                sendFailureType = SendFailureDialogState.SendFailureType.Unknown,
            )
        )
}
