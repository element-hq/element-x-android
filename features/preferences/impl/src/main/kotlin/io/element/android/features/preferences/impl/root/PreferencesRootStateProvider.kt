/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.preferences.impl.root

import io.element.android.features.logout.api.direct.DirectLogoutState
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.ui.strings.CommonStrings

fun aPreferencesRootState() = PreferencesRootState(
    myUser = null,
    version = "Version 1.1 (1)",
    showCompleteVerification = true,
    showSecureBackup = true,
    showSecureBackupBadge = true,
    accountManagementUrl = "aUrl",
    devicesManagementUrl = "anOtherUrl",
    showAnalyticsSettings = true,
    showDeveloperSettings = true,
    showNotificationSettings = true,
    showLockScreenSettings = true,
    snackbarMessage = SnackbarMessage(CommonStrings.common_verification_complete),
    directLogoutState = aDirectLogoutState(),
)

fun aDirectLogoutState() = DirectLogoutState(
    canDoDirectSignOut = true,
    showConfirmationDialog = false,
    logoutAction = Async.Uninitialized,
    eventSink = {},
)
