/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.reset.password

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction

class ResetIdentityPasswordStateProvider : PreviewParameterProvider<ResetIdentityPasswordState> {
    override val values: Sequence<ResetIdentityPasswordState>
        get() = sequenceOf(
            aResetIdentityPasswordState(),
            aResetIdentityPasswordState(resetAction = AsyncAction.Loading),
            aResetIdentityPasswordState(resetAction = AsyncAction.Success(Unit)),
            aResetIdentityPasswordState(resetAction = AsyncAction.Failure(IllegalStateException("Failed"))),
        )
}

private fun aResetIdentityPasswordState(
    resetAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (ResetIdentityPasswordEvent) -> Unit = {},
) = ResetIdentityPasswordState(
    resetAction = resetAction,
    eventSink = eventSink,
)
