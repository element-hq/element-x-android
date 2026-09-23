/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.account

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.logout.api.direct.DirectLogoutState
import io.element.android.features.logout.api.direct.aDirectLogoutState
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.ui.strings.CommonStrings

open class PreferencesAccountStatePreviewParam : PreviewParameterProvider<PreferencesAccountState> {
    override val values: Sequence<PreferencesAccountState>
        get() = sequenceOf(
            // Nominal state, that a regular user will see if multi account is enabled
            aPreferencesAccountState(
                myUser = aMatrixUser(avatarUrl = "anAvatarUrl"),
                showSecureBackup = true,
                accountManagementUrl = "aUrl",
                canReportBug = true,
                showLinkNewDevice = true,
                canDeactivateAccount = false,
            ),
            aPreferencesAccountState(
                myUser = aMatrixUser(displayName = null),
                showSecureBackup = true,
                canDeactivateAccount = true,
            ),
            aPreferencesAccountState(
                accountManagementUrl = "aUrl",
                showSecureBackup = true,
                showSecureBackupBadge = true,
            ),
            aPreferencesAccountState(
                canReportBug = true,
                snackbarMessage = SnackbarMessage(CommonStrings.common_verification_complete),
            ),
            aPreferencesAccountState(
                showLinkNewDevice = true,
                canDeactivateAccount = true,
            ),
        )
}

fun aPreferencesAccountState(
    myUser: MatrixUser = aMatrixUser(),
    showSecureBackup: Boolean = false,
    showSecureBackupBadge: Boolean = false,
    accountManagementUrl: String? = null,
    canReportBug: Boolean = false,
    showLinkNewDevice: Boolean = false,
    canDeactivateAccount: Boolean = false,
    directLogoutState: DirectLogoutState = aDirectLogoutState(),
    snackbarMessage: SnackbarMessage? = null,
) = PreferencesAccountState(
    myUser = myUser,
    showSecureBackup = showSecureBackup,
    showSecureBackupBadge = showSecureBackupBadge,
    accountManagementUrl = accountManagementUrl,
    canReportBug = canReportBug,
    showLinkNewDevice = showLinkNewDevice,
    canDeactivateAccount = canDeactivateAccount,
    directLogoutState = directLogoutState,
    snackbarMessage = snackbarMessage,
)
