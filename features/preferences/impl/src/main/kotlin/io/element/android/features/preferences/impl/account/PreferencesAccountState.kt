/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.account

import io.element.android.features.logout.api.direct.DirectLogoutState
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.matrix.api.user.MatrixUser

data class PreferencesAccountState(
    val myUser: MatrixUser,
    val showSecureBackup: Boolean,
    val showSecureBackupBadge: Boolean,
    val accountManagementUrl: String?,
    val canReportBug: Boolean,
    val showLinkNewDevice: Boolean,
    val canDeactivateAccount: Boolean,
    val numberOfBlockedUsers: Int,
    val directLogoutState: DirectLogoutState,
    val snackbarMessage: SnackbarMessage?,
) {
    val showBlockedUsersItem = numberOfBlockedUsers > 0
}
