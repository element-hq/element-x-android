/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.bugreport

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction

open class BugReportStatePreviewParam : PreviewParameterProvider<BugReportState> {
    override val values: Sequence<BugReportState>
        get() = sequenceOf(
            aBugReportState(),
            aBugReportState(
                formState = BugReportFormState.Default.copy(
                    description = "A long enough description",
                    sendScreenshot = true,
                ),
                hasCrashLogs = true,
                screenshotUri = "aUri",
            ),
            aBugReportState(sending = AsyncAction.Loading),
            aBugReportState(sending = AsyncAction.Success(Unit)),
            aBugReportState(sending = AsyncAction.Failure(BugReportFormError.DescriptionTooShort)),
        )
}

private fun aBugReportState(
    formState: BugReportFormState = BugReportFormState.Default,
    hasCrashLogs: Boolean = false,
    screenshotUri: String? = null,
    sendingProgress: Float = 0F,
    sending: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (BugReportEvent) -> Unit = {},
) = BugReportState(
    formState = formState,
    hasCrashLogs = hasCrashLogs,
    screenshotUri = screenshotUri,
    sendingProgress = sendingProgress,
    sending = sending,
    eventSink = eventSink,
)
