/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.securityandprivacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.roomdetails.impl.R
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.coverage.ExcludeFromCoverage
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.preview.PreviewWithLargeHeight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.ui.strings.CommonStrings
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

@Composable
fun SecurityAndPrivacyView(
    state: SecurityAndPrivacyState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            SecurityAndPrivacyToolbar(
                isSaveActionEnabled = state.canBeSaved,
                onBackClick = onBackClick,
                onSaveClick = {
                    state.eventSink(SecurityAndPrivacyEvents.Save)
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .consumeWindowInsets(padding),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            RoomAccessSection(
                modifier = Modifier.padding(top = 24.dp),
                selected = state.currentSettings.roomAccess,
                onSelected = { state.eventSink(SecurityAndPrivacyEvents.ChangeRoomAccess(it)) },
            )
            if (state.showRoomVisibilitySections) {
                RoomVisibilitySection(state.homeserverName)
                RoomAddressSection(
                    roomAddress = state.currentSettings.formattedAddress,
                    homeserverName = state.homeserverName,
                    onRoomAddressClick = { },
                    isVisibleInPublicDirectory = state.currentSettings.isVisibleInRoomDirectory,
                    onVisibilityChange = { },
                )
            }
            EncryptionSection(
                isEncryptionEnabled = state.currentSettings.isEncrypted,
                onEnableEncryption = { state.eventSink(SecurityAndPrivacyEvents.EnableEncryption) },
            )
            if (state.showRoomHistoryVisibilitySection) {
                RoomHistorySection(
                    selectedOption = state.currentSettings.historyVisibility.get(),
                    availableOptions = state.availableHistoryVisibilities,
                    onSelected = { state.eventSink(SecurityAndPrivacyEvents.ChangeHistoryVisibility(it)) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecurityAndPrivacyToolbar(
    isSaveActionEnabled: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.screen_room_details_security_and_privacy_title),
                style = ElementTheme.typography.aliasScreenTitle,
            )
        },
        navigationIcon = { BackButton(onClick = onBackClick) },
        actions = {
            TextButton(
                text = stringResource(CommonStrings.action_save),
                enabled = isSaveActionEnabled,
                onClick = onSaveClick,
            )
        }
    )
}

@Composable
private fun SecurityAndPrivacySection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.selectableGroup()
    ) {
        Text(
            text = title,
            style = ElementTheme.typography.fontBodyLgMedium,
            color = ElementTheme.colors.textPrimary,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        content()
    }
}

@Composable
private fun RoomAccessSection(
    selected: SecurityAndPrivacyRoomAccess,
    onSelected: (SecurityAndPrivacyRoomAccess) -> Unit,
    modifier: Modifier = Modifier,
) {
    SecurityAndPrivacySection(
        title = stringResource(CommonStrings.screen_security_and_privacy_room_access_section_header),
        modifier = modifier,
    ) {
        ListItem(
            headlineContent = { Text(text = stringResource(CommonStrings.screen_security_and_privacy_room_access_invite_only_option_title)) },
            supportingContent = { Text(text = stringResource(CommonStrings.screen_security_and_privacy_room_access_invite_only_option_description)) },
            trailingContent = ListItemContent.RadioButton(selected = selected == SecurityAndPrivacyRoomAccess.InviteOnly),
            onClick = { onSelected(SecurityAndPrivacyRoomAccess.InviteOnly) },
        )
        ListItem(
            headlineContent = { Text(text = stringResource(CommonStrings.screen_security_and_privacy_ask_to_join_option_title)) },
            supportingContent = { Text(text = stringResource(CommonStrings.screen_security_and_privacy_ask_to_join_option_description)) },
            trailingContent = ListItemContent.RadioButton(selected = selected == SecurityAndPrivacyRoomAccess.AskToJoin),
            onClick = { onSelected(SecurityAndPrivacyRoomAccess.AskToJoin) },
        )
        ListItem(
            headlineContent = { Text(text = stringResource(CommonStrings.screen_security_and_privacy_room_access_anyone_option_title)) },
            supportingContent = { Text(text = stringResource(CommonStrings.screen_security_and_privacy_room_access_anyone_option_description)) },
            trailingContent = ListItemContent.RadioButton(selected = selected == SecurityAndPrivacyRoomAccess.Anyone),
            onClick = { onSelected(SecurityAndPrivacyRoomAccess.Anyone) },
        )
        if (selected == SecurityAndPrivacyRoomAccess.SpaceMember) {
            ListItem(
                headlineContent = { Text(text = stringResource(CommonStrings.screen_security_and_privacy_room_access_space_members_option_title)) },
                supportingContent = { Text(text = stringResource(CommonStrings.screen_security_and_privacy_room_access_space_members_option_description)) },
                trailingContent = ListItemContent.RadioButton(selected = true, enabled = false),
            )
        }
    }
}

@Composable
private fun RoomVisibilitySection(
    homeserverName: String,
    modifier: Modifier = Modifier,
) {
    SecurityAndPrivacySection(
        title = stringResource(CommonStrings.screen_security_and_privacy_room_visibility_section_header),
        modifier = modifier,
    ) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(CommonStrings.screen_security_and_privacy_room_visibility_section_footer, homeserverName),
            style = ElementTheme.typography.fontBodyMdRegular,
            color = ElementTheme.colors.textSecondary,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@Composable
private fun RoomAddressSection(
    roomAddress: Optional<String>,
    homeserverName: String,
    isVisibleInPublicDirectory: Optional<AsyncData<Boolean>>,
    onRoomAddressClick: () -> Unit,
    onVisibilityChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    SecurityAndPrivacySection(
        title = stringResource(CommonStrings.screen_security_and_privacy_room_address_section_header),
        modifier = modifier,
    ) {
        ListItem(
            headlineContent = {
                Text(text = roomAddress.getOrNull() ?: stringResource(CommonStrings.screen_security_and_privacy_add_room_address_action))
            },
            trailingContent = if (roomAddress.isEmpty) ListItemContent.Icon(IconSource.Vector(CompoundIcons.Plus())) else null,
            supportingContent = { Text(text = stringResource(CommonStrings.screen_security_and_privacy_room_address_section_footer)) },
            onClick = onRoomAddressClick,
            colors = ListItemDefaults.colors(trailingIconColor = ElementTheme.colors.iconAccentPrimary),
            alwaysClickable = true
        )
        if (isVisibleInPublicDirectory.isPresent) {
            ListItem(
                headlineContent = { Text(text = stringResource(CommonStrings.screen_security_and_privacy_room_directory_visibility_toggle_title)) },
                supportingContent = {
                    Text(text = stringResource(CommonStrings.screen_security_and_privacy_room_directory_visibility_section_footer, homeserverName))
                },
                trailingContent =
                when (val isVisible = isVisibleInPublicDirectory.get()) {
                    is AsyncData.Uninitialized, is AsyncData.Loading -> {
                        ListItemContent.Custom {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .progressSemantics()
                                    .size(20.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    is AsyncData.Failure -> {
                        ListItemContent.Switch(
                            checked = false,
                            enabled = false,
                        )
                    }
                    is AsyncData.Success -> {
                        ListItemContent.Switch(
                            checked = isVisible.data,
                            onChange = onVisibilityChange
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun EncryptionSection(
    isEncryptionEnabled: Boolean,
    onEnableEncryption: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SecurityAndPrivacySection(
        title = stringResource(CommonStrings.screen_security_and_privacy_encryption_section_header),
        modifier = modifier,
    ) {
        ListItem(
            headlineContent = { Text(text = stringResource(CommonStrings.screen_security_and_privacy_encryption_toggle_title)) },
            supportingContent = { Text(text = stringResource(CommonStrings.screen_security_and_privacy_encryption_section_footer)) },
            trailingContent = ListItemContent.Switch(
                checked = isEncryptionEnabled,
                enabled = !isEncryptionEnabled,
                onChange = { onEnableEncryption() }
            ),
        )
    }
}

@Composable
private fun RoomHistorySection(
    selectedOption: SecurityAndPrivacyHistoryVisibility,
    availableOptions: Set<SecurityAndPrivacyHistoryVisibility>,
    onSelected: (SecurityAndPrivacyHistoryVisibility) -> Unit,
    modifier: Modifier = Modifier,
) {
    SecurityAndPrivacySection(
        title = stringResource(CommonStrings.screen_security_and_privacy_room_history_section_header),
        modifier = modifier,
    ) {
        Spacer(Modifier.height(16.dp))
        for (availableOption in availableOptions) {
            val isSelected = availableOption == selectedOption
            when (availableOption) {
                SecurityAndPrivacyHistoryVisibility.SinceSelection -> {
                    ListItem(
                        headlineContent = { Text(text = stringResource(CommonStrings.screen_security_and_privacy_room_history_since_selecting_option_title)) },
                        trailingContent = ListItemContent.RadioButton(selected = isSelected),
                        onClick = { onSelected(availableOption) },
                    )
                }
                SecurityAndPrivacyHistoryVisibility.SinceInvite -> {
                    ListItem(
                        headlineContent = { Text(text = stringResource(CommonStrings.screen_security_and_privacy_room_history_since_invite_option_title)) },
                        trailingContent = ListItemContent.RadioButton(selected = isSelected),
                        onClick = { onSelected(availableOption) },
                    )
                }
                SecurityAndPrivacyHistoryVisibility.Anyone -> {
                    ListItem(
                        headlineContent = { Text(text = stringResource(CommonStrings.screen_security_and_privacy_room_history_anyone_option_title)) },
                        trailingContent = ListItemContent.RadioButton(selected = isSelected),
                        onClick = { onSelected(availableOption) },
                    )
                }
            }
        }
    }
}

@PreviewWithLargeHeight
@Composable
internal fun SecurityAndPrivacyViewLightPreview(@PreviewParameter(SecurityAndPrivacyStateProvider::class) state: SecurityAndPrivacyState) =
    ElementPreviewLight { ContentToPreview(state) }

@PreviewWithLargeHeight
@Composable
internal fun SecurityAndPrivacyViewDarkPreview(@PreviewParameter(SecurityAndPrivacyStateProvider::class) state: SecurityAndPrivacyState) =
    ElementPreviewDark { ContentToPreview(state) }

@ExcludeFromCoverage
@Composable
private fun ContentToPreview(state: SecurityAndPrivacyState) {
    SecurityAndPrivacyView(
        state = state,
        onBackClick = {},
    )
}

