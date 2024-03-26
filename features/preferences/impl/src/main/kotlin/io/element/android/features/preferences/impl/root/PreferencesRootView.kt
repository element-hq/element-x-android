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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.preferences.impl.R
import io.element.android.features.preferences.impl.user.UserPreferences
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.preview.PreviewWithLargeHeight
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarHost
import io.element.android.libraries.designsystem.utils.snackbar.rememberSnackbarHostState
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.MatrixUserProvider
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun PreferencesRootView(
    state: PreferencesRootState,
    onBackPressed: () -> Unit,
    onVerifyClicked: () -> Unit,
    onSecureBackupClicked: () -> Unit,
    onManageAccountClicked: (url: String) -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenRageShake: () -> Unit,
    onOpenLockScreenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenDeveloperSettings: () -> Unit,
    onOpenAdvancedSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenUserProfile: (MatrixUser) -> Unit,
    onOpenBlockedUsers: () -> Unit,
    onSignOutClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = state.snackbarMessage)

    // Include pref from other modules
    PreferencePage(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = stringResource(id = CommonStrings.common_settings),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        UserPreferences(
            modifier = Modifier.clickable {
                onOpenUserProfile(state.myUser)
            },
            user = state.myUser,
        )
        if (state.showSecureBackup) {
            ListItem(
                headlineContent = { Text(stringResource(id = CommonStrings.common_chat_backup)) },
                leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.KeySolid())),
                trailingContent = ListItemContent.Badge.takeIf { state.showSecureBackupBadge },
                onClick = onSecureBackupClicked,
            )
            HorizontalDivider()
        }
        if (state.accountManagementUrl != null) {
            ListItem(
                headlineContent = { Text(stringResource(id = CommonStrings.action_manage_account)) },
                leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.UserProfile())),
                trailingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.PopOut())),
                onClick = { onManageAccountClicked(state.accountManagementUrl) },
            )
            HorizontalDivider()
        }
        if (state.showAnalyticsSettings) {
            ListItem(
                headlineContent = { Text(stringResource(id = CommonStrings.common_analytics)) },
                leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Chart())),
                onClick = onOpenAnalytics,
            )
        }
        if (state.showNotificationSettings) {
            ListItem(
                headlineContent = { Text(stringResource(id = R.string.screen_notification_settings_title)) },
                leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Notifications())),
                onClick = onOpenNotificationSettings,
            )
        }
        if (state.showBlockedUsersItem) {
            ListItem(
                headlineContent = { Text(stringResource(id = CommonStrings.common_blocked_users)) },
                leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Block())),
                onClick = onOpenBlockedUsers,
            )
        }
        ListItem(
            headlineContent = { Text(stringResource(id = CommonStrings.common_report_a_problem)) },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.ChatProblem())),
            onClick = onOpenRageShake
        )
        ListItem(
            headlineContent = { Text(stringResource(id = CommonStrings.common_about)) },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Info())),
            onClick = onOpenAbout,
        )
        if (state.showLockScreenSettings) {
            ListItem(
                headlineContent = { Text(stringResource(id = CommonStrings.common_screen_lock)) },
                leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Lock())),
                onClick = onOpenLockScreenSettings,
            )
        }
        HorizontalDivider()
        if (state.devicesManagementUrl != null) {
            ListItem(
                headlineContent = { Text(stringResource(id = CommonStrings.action_manage_devices)) },
                leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Devices())),
                trailingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.PopOut())),
                onClick = { onManageAccountClicked(state.devicesManagementUrl) },
            )
            HorizontalDivider()
        }
        ListItem(
            headlineContent = { Text(stringResource(id = CommonStrings.common_advanced_settings)) },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Settings())),
            onClick = onOpenAdvancedSettings,
        )
        if (state.showDeveloperSettings) {
            DeveloperPreferencesView(onOpenDeveloperSettings)
        }
        HorizontalDivider()
        ListItem(
            headlineContent = { Text(stringResource(id = CommonStrings.action_signout)) },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.SignOut())),
            style = ListItemStyle.Destructive,
            onClick = onSignOutClicked,
        )
        Footer(
            version = state.version,
            deviceId = state.deviceId,
        )
    }
}

@Composable
private fun Footer(
    version: String,
    deviceId: String?
) {
    val text = remember(version, deviceId) {
        buildString {
            append(version)
            if (deviceId != null) {
                append("\n")
                append(deviceId)
            }
        }
    }
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 24.dp),
        textAlign = TextAlign.Center,
        text = text,
        style = ElementTheme.typography.fontBodySmRegular,
        color = ElementTheme.materialColors.secondary,
    )
}

@Composable
private fun DeveloperPreferencesView(onOpenDeveloperSettings: () -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(id = CommonStrings.common_developer_options)) },
        leadingContent = ListItemContent.Icon(IconSource.Resource(CommonDrawables.ic_developer_options)),
        onClick = onOpenDeveloperSettings
    )
}

@PreviewWithLargeHeight
@Composable
internal fun PreferencesRootViewLightPreview(@PreviewParameter(MatrixUserProvider::class) matrixUser: MatrixUser) =
    ElementPreviewLight { ContentToPreview(matrixUser) }

@PreviewWithLargeHeight
@Composable
internal fun PreferencesRootViewDarkPreview(@PreviewParameter(MatrixUserProvider::class) matrixUser: MatrixUser) =
    ElementPreviewDark { ContentToPreview(matrixUser) }

@Composable
private fun ContentToPreview(matrixUser: MatrixUser) {
    PreferencesRootView(
        state = aPreferencesRootState(myUser = matrixUser),
        onBackPressed = {},
        onOpenAnalytics = {},
        onOpenRageShake = {},
        onOpenDeveloperSettings = {},
        onOpenAdvancedSettings = {},
        onOpenAbout = {},
        onVerifyClicked = {},
        onSecureBackupClicked = {},
        onManageAccountClicked = {},
        onOpenNotificationSettings = {},
        onOpenLockScreenSettings = {},
        onOpenUserProfile = {},
        onOpenBlockedUsers = {},
        onSignOutClicked = {},
    )
}
