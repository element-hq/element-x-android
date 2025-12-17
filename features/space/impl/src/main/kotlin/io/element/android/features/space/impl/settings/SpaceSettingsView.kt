/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.space.impl.R
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun SpaceSettingsView(
    state: SpaceSettingsState,
    onBackClick: () -> Unit,
    onSpaceInfoClick: () -> Unit,
    onMembersClick: () -> Unit,
    onRolesAndPermissionsClick: () -> Unit,
    onSecurityAndPrivacyClick: () -> Unit,
    onLeaveSpaceClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            SpaceSettingsTopBar(onBackClick = onBackClick)
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SpaceInfoSection(
                roomId = state.roomId,
                name = state.name,
                avatarUrl = state.avatarUrl,
                canonicalAlias = state.canonicalAlias?.value,
                canEditDetails = state.canEditDetails,
                onSpaceInfoClick = onSpaceInfoClick,
            )
            Section(isVisible = state.showSecurityAndPrivacy, content = {
                SecurityAndPrivacyItem(
                    onClick = onSecurityAndPrivacyClick
                )
            })
            Section(content = {
                MembersItem(state.memberCount, onClick = onMembersClick)
                if (state.showRolesAndPermissions) {
                    RolesAndPermissionsItem(onClick = onRolesAndPermissionsClick)
                }
            })
            Section(content = {
                LeaveSpaceItem(
                    onClick = onLeaveSpaceClick
                )
            })
        }
    }
}

@Composable
private fun SpaceInfoSection(
    roomId: RoomId,
    name: String,
    avatarUrl: String?,
    canonicalAlias: String?,
    canEditDetails: Boolean,
    onSpaceInfoClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = canEditDetails, onClick = onSpaceInfoClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            avatarData = AvatarData(roomId.value, name, avatarUrl, AvatarSize.SpaceListItem),
            avatarType = AvatarType.Space(),
            contentDescription = avatarUrl?.let { stringResource(CommonStrings.a11y_avatar) },
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = name,
                style = ElementTheme.typography.fontHeadingMdRegular,
                color = ElementTheme.colors.textPrimary,
            )
            if (canonicalAlias != null) {
                Text(
                    text = canonicalAlias,
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = ElementTheme.colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun Section(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (isVisible) {
        PreferenceCategory(content = content, modifier = modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpaceSettingsTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        titleStr = stringResource(CommonStrings.common_settings),
        navigationIcon = { BackButton(onClick = onBackClick) },
        modifier = modifier,
    )
}

@Composable
private fun SecurityAndPrivacyItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.screen_space_settings_security_and_privacy)) },
        leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Lock())),
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun MembersItem(
    memberCount: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = { Text(stringResource(CommonStrings.common_people)) },
        leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.User())),
        trailingContent = ListItemContent.Text(memberCount.toString()),
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun RolesAndPermissionsItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.screen_space_settings_roles_and_permissions)) },
        leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Admin())),
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun LeaveSpaceItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = {
            Text(stringResource(CommonStrings.action_leave_space))
        },
        leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Leave())),
        style = ListItemStyle.Destructive,
        onClick = onClick,
        modifier = modifier,
    )
}

@PreviewsDayNight
@Composable
internal fun SpaceSettingsViewPreview(
    @PreviewParameter(SpaceSettingsStateProvider::class) state: SpaceSettingsState
) = ElementPreview {
    SpaceSettingsView(
        state = state,
        onBackClick = {},
        onSpaceInfoClick = {},
        onMembersClick = {},
        onRolesAndPermissionsClick = {},
        onSecurityAndPrivacyClick = {},
        onLeaveSpaceClick = {},
        modifier = Modifier,
    )
}
