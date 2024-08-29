/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.root

import io.element.android.features.logout.api.direct.aDirectLogoutState
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.ui.strings.CommonStrings

fun aPreferencesRootState(
    myUser: MatrixUser,
    eventSink: (PreferencesRootEvents) -> Unit = { _ -> },
) = PreferencesRootState(
    isDebugBuild = false,
    myUser = myUser,
    version = "Version 1.1 (1)",
    deviceId = DeviceId("ILAKNDNASDLK"),
    showSecureBackup = true,
    showSecureBackupBadge = true,
    accountManagementUrl = "aUrl",
    devicesManagementUrl = "anOtherUrl",
    showAnalyticsSettings = true,
    showDeveloperSettings = true,
    showNotificationSettings = true,
    showLockScreenSettings = true,
    showBlockedUsersItem = true,
    canDeactivateAccount = true,
    snackbarMessage = SnackbarMessage(CommonStrings.common_verification_complete),
    directLogoutState = aDirectLogoutState(),
    eventSink = eventSink,
)
