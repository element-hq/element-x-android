/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.reportroom.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction

open class ReportRoomStateProvider : PreviewParameterProvider<ReportRoomState> {
    override val values: Sequence<ReportRoomState>
        get() = sequenceOf(
            aReportRoomState(),
            aReportRoomState(reason = "Inappropriate content"),
            aReportRoomState(leaveRoom = true),
            aReportRoomState(reportAction = AsyncAction.Loading),
            aReportRoomState(reportAction = AsyncAction.Failure(Exception("Failed to report"))),
        )
}

fun aReportRoomState(
    reason: String = "",
    leaveRoom: Boolean = false,
    reportAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (ReportRoomEvents) -> Unit = {}
) = ReportRoomState(
    reason = reason,
    leaveRoom = leaveRoom,
    reportAction = reportAction,
    eventSink = eventSink,
)
