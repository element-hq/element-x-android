/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.enable

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction

open class SecureBackupEnableStateProvider : PreviewParameterProvider<SecureBackupEnableState> {
    override val values: Sequence<SecureBackupEnableState>
        get() = sequenceOf(
            aSecureBackupEnableState(),
            aSecureBackupEnableState(enableAction = AsyncAction.Loading),
            aSecureBackupEnableState(enableAction = AsyncAction.Failure(Exception("Failed to enable"))),
            // Add other states here
        )
}

fun aSecureBackupEnableState(
    enableAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
) = SecureBackupEnableState(
    enableAction = enableAction,
    eventSink = {}
)
