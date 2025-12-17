/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.enter

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyUserStory
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyViewState
import io.element.android.features.securebackup.impl.setup.views.aFormattedRecoveryKey
import io.element.android.libraries.architecture.AsyncAction

open class SecureBackupEnterRecoveryKeyStateProvider : PreviewParameterProvider<SecureBackupEnterRecoveryKeyState> {
    override val values: Sequence<SecureBackupEnterRecoveryKeyState>
        get() = sequenceOf(
            aSecureBackupEnterRecoveryKeyState(recoveryKey = ""),
            aSecureBackupEnterRecoveryKeyState(),
            aSecureBackupEnterRecoveryKeyState(submitAction = AsyncAction.Loading),
            aSecureBackupEnterRecoveryKeyState(submitAction = AsyncAction.Failure(Exception("A Failure"))),
            aSecureBackupEnterRecoveryKeyState(displayTextFieldContents = false),
        )
}

fun aSecureBackupEnterRecoveryKeyState(
    recoveryKey: String = aFormattedRecoveryKey(),
    isSubmitEnabled: Boolean = recoveryKey.isNotEmpty(),
    displayTextFieldContents: Boolean = true,
    submitAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (SecureBackupEnterRecoveryKeyEvents) -> Unit = {},
) = SecureBackupEnterRecoveryKeyState(
    recoveryKeyViewState = RecoveryKeyViewState(
        recoveryKeyUserStory = RecoveryKeyUserStory.Enter,
        formattedRecoveryKey = recoveryKey,
        displayTextFieldContents = displayTextFieldContents,
        inProgress = submitAction.isLoading(),
    ),
    isSubmitEnabled = isSubmitEnabled,
    submitAction = submitAction,
    eventSink = eventSink,
)
