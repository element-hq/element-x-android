/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.report

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction

open class ReportMessageStateProvider : PreviewParameterProvider<ReportMessageState> {
    override val values: Sequence<ReportMessageState>
        get() = sequenceOf(
            aReportMessageState(),
            aReportMessageState(reason = "This user is making the chat very toxic."),
            aReportMessageState(reason = "This user is making the chat very toxic.", blockUser = true),
            aReportMessageState(reason = "This user is making the chat very toxic.", blockUser = true, result = AsyncAction.Loading),
            aReportMessageState(reason = "This user is making the chat very toxic.", blockUser = true, result = AsyncAction.Failure(Throwable("error"))),
            aReportMessageState(reason = "This user is making the chat very toxic.", blockUser = true, result = AsyncAction.Success(Unit)),
            // Add other states here
        )
}

fun aReportMessageState(
    reason: String = "",
    blockUser: Boolean = false,
    result: AsyncAction<Unit> = AsyncAction.Uninitialized,
) = ReportMessageState(
    reason = reason,
    blockUser = blockUser,
    result = result,
    eventSink = {}
)
