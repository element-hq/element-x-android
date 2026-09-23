/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.account

import io.element.android.libraries.matrix.api.user.MatrixUser

interface PreferencesAccountCallback {
    fun navigateToBugReport()
    fun navigateToSecureBackup()
    fun navigateToModerationAndSafety()
    fun navigateToNotificationSettings()
    fun navigateToLinkNewDevice()
    fun navigateToUserProfile(matrixUser: MatrixUser)
    fun navigateToBlockedUsers()
    fun startSignOutFlow()
    fun startAccountDeactivationFlow()
}
