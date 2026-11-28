/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.check

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyUserStory
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyViewState

open class SecureBackupCheckKeyStatePreviewParam : PreviewParameterProvider<SecureBackupCheckKeyState> {
    override val values: Sequence<SecureBackupCheckKeyState>
        get() = sequenceOf(
            aSecureBackupCheckKeyState(),
            aSecureBackupCheckKeyState(recoveryKey = "aRecoveryKey", isSubmitEnabled = true),
            aSecureBackupCheckKeyState(recoveryKey = "aRecoveryKey", inProgress = true),
            aSecureBackupCheckKeyState(recoveryKey = "aWrongRecoveryKey", isKeyIncorrect = true),
            aSecureBackupCheckKeyState(recoveryKey = "aRecoveryKey", isCompleted = true),
        )
}

fun aSecureBackupCheckKeyState(
    recoveryKey: String = "",
    displayTextFieldContents: Boolean = false,
    inProgress: Boolean = false,
    isSubmitEnabled: Boolean = false,
    isKeyIncorrect: Boolean = false,
    isCompleted: Boolean = false,
    eventSink: (SecureBackupCheckKeyEvent) -> Unit = {},
) = SecureBackupCheckKeyState(
    recoveryKeyViewState = RecoveryKeyViewState(
        recoveryKeyUserStory = RecoveryKeyUserStory.Enter,
        formattedRecoveryKey = recoveryKey,
        displayTextFieldContents = displayTextFieldContents,
        inProgress = inProgress,
    ),
    isSubmitEnabled = isSubmitEnabled,
    isKeyIncorrect = isKeyIncorrect,
    isCompleted = isCompleted,
    eventSink = eventSink,
)
