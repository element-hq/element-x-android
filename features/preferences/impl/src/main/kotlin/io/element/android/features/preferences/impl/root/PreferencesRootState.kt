/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.root

import io.element.android.features.logout.api.direct.DirectLogoutState
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList

data class PreferencesRootState(
    val myUser: MatrixUser,
    val version: String,
    val deviceId: DeviceId?,
    val isMultiAccountEnabled: Boolean,
    val otherSessions: ImmutableList<MatrixUser>,
    val showSecureBackup: Boolean,
    val showSecureBackupBadge: Boolean,
    val accountManagementUrl: String?,
    val devicesManagementUrl: String?,
    val canReportBug: Boolean,
    val showAnalyticsSettings: Boolean,
    val showDeveloperSettings: Boolean,
    val canDeactivateAccount: Boolean,
    val showBlockedUsersItem: Boolean,
    val showLabsItem: Boolean,
    val directLogoutState: DirectLogoutState,
    val snackbarMessage: SnackbarMessage?,
    val eventSink: (PreferencesRootEvents) -> Unit,
)
