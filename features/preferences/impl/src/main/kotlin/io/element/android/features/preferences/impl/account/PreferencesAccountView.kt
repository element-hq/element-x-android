/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun PreferencesAccountView(
    state: PreferencesAccountState,
    onSecureBackupClick: () -> Unit,
    onManageAccountClick: (url: String) -> Unit,
    onLinkNewDeviceClick: () -> Unit,
    onOpenRageShake: () -> Unit,
    onModerationAndSafetyClick: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenBlockedUsers: () -> Unit,
    onSignOutClick: () -> Unit,
    onDeactivateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // 'Account settings' section
        ManageAccountSection(
            state = state,
            onManageAccountClick = onManageAccountClick,
            onLinkNewDeviceClick = onLinkNewDeviceClick,
            onOpenBlockedUsers = onOpenBlockedUsers,
        )
        OtherSettingsSection(
            state = state,
            onOpenNotificationSettings = onOpenNotificationSettings,
            onSecureBackupClick = onSecureBackupClick,
        )
        // General section
        GeneralSection(
            state = state,
            onModerationAndSafetyClick = onModerationAndSafetyClick,
            onOpenRageShake = onOpenRageShake,
            onSignOutClick = onSignOutClick,
            onDeactivateClick = onDeactivateClick,
        )
    }
}

@Composable
private fun ManageAccountSection(
    state: PreferencesAccountState,
    onManageAccountClick: (url: String) -> Unit,
    onLinkNewDeviceClick: () -> Unit,
    onOpenBlockedUsers: () -> Unit,
) {
    PreferenceCategory(
        title = stringResource(CommonStrings.common_account_settings),
        showTopDivider = false,
    ) {
        state.accountManagementUrl?.let { url ->
            ListItem(
                content = { Text(stringResource(id = CommonStrings.action_manage_account_and_devices)) },
                leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.UserProfile())),
                trailingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.PopOut())),
                onClick = { onManageAccountClick(url) },
            )
        }
        if (state.showLinkNewDevice) {
            ListItem(
                content = { Text(stringResource(id = CommonStrings.common_link_new_device)) },
                leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Devices())),
                onClick = onLinkNewDeviceClick,
            )
        }
        if (state.showBlockedUsersItem) {
            ListItem(
                content = { Text(stringResource(id = CommonStrings.common_blocked_users)) },
                leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Block())),
                onClick = onOpenBlockedUsers,
                trailingContent = ListItemContent.Text(state.numberOfBlockedUsers.toString()),
            )
        }
    }
}

@Composable
private fun ColumnScope.OtherSettingsSection(
    state: PreferencesAccountState,
    onOpenNotificationSettings: () -> Unit,
    onSecureBackupClick: () -> Unit,
) {
    ListItem(
        content = { Text(stringResource(id = R.string.screen_notification_settings_title)) },
        leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Notifications())),
        onClick = onOpenNotificationSettings,
    )
    if (state.showSecureBackup) {
        ListItem(
            content = { Text(stringResource(id = CommonStrings.common_encryption)) },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Key())),
            trailingContent = ListItemContent.Badge.takeIf { state.showSecureBackupBadge },
            onClick = onSecureBackupClick,
        )
    }
}

@Composable
private fun ColumnScope.GeneralSection(
    state: PreferencesAccountState,
    onModerationAndSafetyClick: () -> Unit,
    onOpenRageShake: () -> Unit,
    onSignOutClick: () -> Unit,
    onDeactivateClick: () -> Unit,
) {
    HorizontalDivider()
    ListItem(
        content = { Text(stringResource(id = CommonStrings.common_moderation_and_safety)) },
        leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Admin())),
        onClick = onModerationAndSafetyClick,
    )

    if (state.canReportBug) {
        ListItem(
            content = { Text(stringResource(id = CommonStrings.common_report_a_problem)) },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.ChatProblem())),
            onClick = onOpenRageShake,
        )
    }
    HorizontalDivider()
    ListItem(
        content = { Text(stringResource(id = CommonStrings.action_signout)) },
        leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Close())),
        style = ListItemStyle.Destructive,
        onClick = onSignOutClick,
    )
    if (state.canDeactivateAccount) {
        ListItem(
            content = { Text(stringResource(id = CommonStrings.action_delete_account)) },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Delete())),
            style = ListItemStyle.Destructive,
            onClick = onDeactivateClick,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun PreferencesAccountViewPreview(@PreviewParameter(PreferencesAccountStatePreviewParam::class) state: PreferencesAccountState) =
    ElementPreview {
        PreferencesAccountView(
            state = state,
            onModerationAndSafetyClick = {},
            onOpenRageShake = {},
            onSecureBackupClick = {},
            onManageAccountClick = {},
            onLinkNewDeviceClick = {},
            onOpenNotificationSettings = {},
            onOpenBlockedUsers = {},
            onSignOutClick = {},
            onDeactivateClick = {},
        )
    }

