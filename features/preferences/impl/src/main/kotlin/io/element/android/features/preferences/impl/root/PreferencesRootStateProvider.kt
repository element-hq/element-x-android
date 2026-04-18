/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.root

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.logout.api.direct.DirectLogoutState
import io.element.android.features.logout.api.direct.aDirectLogoutState
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.toImmutableList

open class PreferencesRootStateProvider : PreviewParameterProvider<PreferencesRootState> {
    override val values: Sequence<PreferencesRootState>
        get() = sequenceOf(
            // Nominal state, that a regular user will see if multi account is enabled
            aPreferencesRootState(
                myUser = aMatrixUser(avatarUrl = "anAvatarUrl"),
                version = "Version 1.1 (1)",
                deviceId = DeviceId("ILAKNDNASDLK"),
                isMultiAccountEnabled = true,
                otherSessions = aMatrixUserList().drop(1).take(1),
                showSecureBackup = true,
                accountManagementUrl = "aUrl",
                canReportBug = true,
                showLinkNewDevice = true,
                showAnalyticsSettings = true,
                canDeactivateAccount = false,
                nbOfBlockedUsers = 3,
                showLabsItem = true,
            ),
            aPreferencesRootState(
                myUser = aMatrixUser(displayName = null),
                isMultiAccountEnabled = true,
                showSecureBackup = true,
                canDeactivateAccount = true,
            ),
            aPreferencesRootState(
                isMultiAccountEnabled = true,
                otherSessions = aMatrixUserList().drop(1).take(3),
                accountManagementUrl = "aUrl",
                showSecureBackup = true,
                showSecureBackupBadge = true,
            ),
            aPreferencesRootState(
                deviceId = DeviceId("ILAKNDNASDLK"),
                showLabsItem = true,
                canReportBug = true,
                nbOfBlockedUsers = 3,
                snackbarMessage = SnackbarMessage(CommonStrings.common_verification_complete),
            ),
            aPreferencesRootState(
                showLinkNewDevice = true,
                showAnalyticsSettings = true,
                showDeveloperSettings = true,
                canDeactivateAccount = true,
            ),
            // Minimal state
            aPreferencesRootState(),
        )
}

fun aPreferencesRootState(
    myUser: MatrixUser = aMatrixUser(),
    version: String = "Version 1.1 (1)",
    deviceId: DeviceId? = null,
    isMultiAccountEnabled: Boolean = false,
    otherSessions: List<MatrixUser> = emptyList(),
    showSecureBackup: Boolean = false,
    showSecureBackupBadge: Boolean = false,
    accountManagementUrl: String? = null,
    canReportBug: Boolean = false,
    showLinkNewDevice: Boolean = false,
    showAnalyticsSettings: Boolean = false,
    showDeveloperSettings: Boolean = false,
    canDeactivateAccount: Boolean = false,
    nbOfBlockedUsers: Int = 0,
    showLabsItem: Boolean = false,
    directLogoutState: DirectLogoutState = aDirectLogoutState(),
    snackbarMessage: SnackbarMessage? = null,
    eventSink: (PreferencesRootEvent) -> Unit = {},
) = PreferencesRootState(
    myUser = myUser,
    version = version,
    deviceId = deviceId,
    isMultiAccountEnabled = isMultiAccountEnabled,
    otherSessions = otherSessions.toImmutableList(),
    showSecureBackup = showSecureBackup,
    showSecureBackupBadge = showSecureBackupBadge,
    accountManagementUrl = accountManagementUrl,
    canReportBug = canReportBug,
    showLinkNewDevice = showLinkNewDevice,
    showAnalyticsSettings = showAnalyticsSettings,
    showDeveloperSettings = showDeveloperSettings,
    canDeactivateAccount = canDeactivateAccount,
    nbOfBlockedUsers = nbOfBlockedUsers,
    showLabsItem = showLabsItem,
    directLogoutState = directLogoutState,
    snackbarMessage = snackbarMessage,
    eventSink = eventSink,
)
