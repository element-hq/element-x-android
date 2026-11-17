/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.logout.impl

import io.element.android.libraries.architecture.AsyncAction

data class LogoutState(
    val confirmation: LogoutConfirmation,
    val logoutAction: AsyncAction<Unit>,
    val eventSink: (LogoutEvent) -> Unit,
)

/**
 * What the user is shown before the device is removed, which depends on how much they could lose by it.
 */
sealed interface LogoutConfirmation {
    /** Still working out what the user has to be told. */
    data object Loading : LogoutConfirmation

    /** The user has another verified device, so only a reminder is needed. */
    data object OtherVerifiedDevice : LogoutConfirmation

    /** This is the only verified device, but the user has a recovery key. */
    data object RecoveryKeyOnly : LogoutConfirmation

    /** This is the only verified device, and the user has no recovery key. */
    data object NoRecovery : LogoutConfirmation

    /** The user has a recovery key, but the room keys are not backed up. */
    data object KeyBackupDisabled : LogoutConfirmation

    /** The room keys are still being backed up, which has to finish before the device can be removed. */
    data object BackupOngoing : LogoutConfirmation

    /** The room keys could not finish being backed up because the device is offline. */
    data object BackupOffline : LogoutConfirmation

    /** The user is done with their recovery key and the device is removed once the room keys are backed up. */
    data object WaitingForBackup : LogoutConfirmation
}
