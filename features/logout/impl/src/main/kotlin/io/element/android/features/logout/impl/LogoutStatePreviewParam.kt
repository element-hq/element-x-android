/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.logout.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction

open class LogoutStatePreviewParam : PreviewParameterProvider<LogoutState> {
    override val values: Sequence<LogoutState>
        get() = sequenceOf(
            aLogoutState(confirmation = LogoutConfirmation.OtherVerifiedDevice),
            aLogoutState(confirmation = LogoutConfirmation.RecoveryKeyOnly),
            aLogoutState(confirmation = LogoutConfirmation.NoRecovery),
            aLogoutState(confirmation = LogoutConfirmation.KeyBackupDisabled),
            aLogoutState(confirmation = LogoutConfirmation.BackupOngoing),
            aLogoutState(confirmation = LogoutConfirmation.BackupOffline),
            aLogoutState(confirmation = LogoutConfirmation.Loading),
            aLogoutState(confirmation = LogoutConfirmation.WaitingForBackup),
            aLogoutState(logoutAction = AsyncAction.Loading),
            aLogoutState(logoutAction = AsyncAction.Failure(Exception("Failed to logout"))),
        )
}

fun aLogoutState(
    confirmation: LogoutConfirmation = LogoutConfirmation.OtherVerifiedDevice,
    logoutAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (LogoutEvent) -> Unit = {},
) = LogoutState(
    confirmation = confirmation,
    logoutAction = logoutAction,
    eventSink = eventSink,
)
