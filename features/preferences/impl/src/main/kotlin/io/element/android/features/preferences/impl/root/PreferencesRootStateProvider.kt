/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.root

import io.element.android.features.logout.api.direct.aDirectLogoutState
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.toImmutableList

fun aPreferencesRootState(
    myUser: MatrixUser = aMatrixUser(),
    otherSessions: List<MatrixUser> = emptyList(),
    eventSink: (PreferencesRootEvents) -> Unit = { _ -> },
) = PreferencesRootState(
    myUser = myUser,
    version = "Version 1.1 (1)",
    deviceId = DeviceId("ILAKNDNASDLK"),
    isMultiAccountEnabled = true,
    otherSessions = otherSessions.toImmutableList(),
    showSecureBackup = true,
    showSecureBackupBadge = true,
    accountManagementUrl = "aUrl",
    devicesManagementUrl = "anOtherUrl",
    showAnalyticsSettings = true,
    canReportBug = true,
    showDeveloperSettings = true,
    showBlockedUsersItem = true,
    showLabsItem = true,
    canDeactivateAccount = true,
    snackbarMessage = SnackbarMessage(CommonStrings.common_verification_complete),
    directLogoutState = aDirectLogoutState(),
    eventSink = eventSink,
)
