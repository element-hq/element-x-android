/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.impl.declineandblock

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction

open class DeclineAndBlockStateProvider : PreviewParameterProvider<DeclineAndBlockState> {
    override val values: Sequence<DeclineAndBlockState>
        get() = sequenceOf(
            aDeclineAndBlockState(),
            aDeclineAndBlockState(
                reportRoom = true,
                reportReason = "Inappropriate content",
            ),
            aDeclineAndBlockState(
                blockUser = true,
            ),
            aDeclineAndBlockState(
                declineAction = AsyncAction.Loading,
            ),
            aDeclineAndBlockState(
                declineAction = AsyncAction.Failure(Exception("Failed to decline")),
            ),
        )
}

fun aDeclineAndBlockState(
    reportRoom: Boolean = false,
    reportReason: String = "",
    blockUser: Boolean = false,
    declineAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (DeclineAndBlockEvents) -> Unit = {},
) = DeclineAndBlockState(
    reportRoom = reportRoom,
    reportReason = reportReason,
    blockUser = blockUser,
    declineAction = declineAction,
    eventSink = eventSink,
)
