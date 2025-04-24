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
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.async.AsyncActionViewDefaults
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
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
import kotlinx.collections.immutable.ImmutableSet

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
            if (state.showRoomAccessSection) {
                RoomAccessSection(
                    modifier = Modifier.padding(top = 24.dp),
                    edited = state.editedSettings.roomAccess,
                    saved = state.savedSettings.roomAccess,
                    onSelectOption = { state.eventSink(SecurityAndPrivacyEvents.ChangeRoomAccess(it)) },
                )
            }
            if (state.showRoomVisibilitySections) {
                RoomVisibilitySection(state.homeserverName)
                RoomAddressSection(
                    roomAddress = state.editedSettings.address,
                    homeserverName = state.homeserverName,
                    onRoomAddressClick = { state.eventSink(SecurityAndPrivacyEvents.EditRoomAddress) },
                    isVisibleInRoomDirectory = state.editedSettings.isVisibleInRoomDirectory,
                    onVisibilityChange = {
                        state.eventSink(SecurityAndPrivacyEvents.ToggleRoomVisibility)
                    },
                )
            }
            if (state.showEncryptionSection) {
                EncryptionSection(
                    isRoomEncrypted = state.editedSettings.isEncrypted,
                    // encryption can't be disabled once enabled
                    canToggleEncryption = !state.savedSettings.isEncrypted,
                    onToggleEncryption = { state.eventSink(SecurityAndPrivacyEvents.ToggleEncryptionState) },
                    showConfirmation = state.showEnableEncryptionConfirmation,
                    onDismissConfirmation = { state.eventSink(SecurityAndPrivacyEvents.CancelEnableEncryption) },
                    onConfirmEncryption = { state.eventSink(SecurityAndPrivacyEvents.ConfirmEnableEncryption) },
                )
            }
            if (state.showHistoryVisibilitySection) {
                HistoryVisibilitySection(
                    editedOption = state.editedSettings.historyVisibility,
                    savedOptions = state.savedSettings.historyVisibility,
                    availableOptions = state.availableHistoryVisibilities,
                    onSelectOption = { state.eventSink(SecurityAndPrivacyEvents.ChangeHistoryVisibility(it)) },
                )
            }
        }
    }
    AsyncActionView(
        async = state.saveAction,
        onSuccess = { },
        onErrorDismiss = { state.eventSink(SecurityAndPrivacyEvents.DismissSaveError) },
        errorMessage = { stringResource(CommonStrings.error_unknown) },
        progressDialog = {
            AsyncActionViewDefaults.ProgressDialog(
                progressText = stringResource(CommonStrings.common_saving),
            )
        },
        onRetry = { state.eventSink(SecurityAndPrivacyEvents.Save) },
    )
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
    edited: SecurityAndPrivacyRoomAccess,
    saved: SecurityAndPrivacyRoomAccess,
    onSelectOption: (SecurityAndPrivacyRoomAccess) -> Unit,
    modifier: Modifier = Modifier,
) {
    SecurityAndPrivacySection(
        title = stringResource(R.string.screen_security_and_privacy_room_access_section_header),
        modifier = modifier,
    ) {
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.screen_security_and_privacy_room_access_invite_only_option_title)) },
            supportingContent = { Text(text = stringResource(R.string.screen_security_and_privacy_room_access_invite_only_option_description)) },
            trailingContent = ListItemContent.RadioButton(selected = edited == SecurityAndPrivacyRoomAccess.InviteOnly),
            onClick = { onSelectOption(SecurityAndPrivacyRoomAccess.InviteOnly) },
        )
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.screen_security_and_privacy_ask_to_join_option_title)) },
            supportingContent = { Text(text = stringResource(R.string.screen_security_and_privacy_ask_to_join_option_description)) },
            trailingContent = ListItemContent.RadioButton(selected = edited == SecurityAndPrivacyRoomAccess.AskToJoin),
            onClick = { onSelectOption(SecurityAndPrivacyRoomAccess.AskToJoin) },
        )
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.screen_security_and_privacy_room_access_anyone_option_title)) },
            supportingContent = { Text(text = stringResource(R.string.screen_security_and_privacy_room_access_anyone_option_description)) },
            trailingContent = ListItemContent.RadioButton(selected = edited == SecurityAndPrivacyRoomAccess.Anyone),
            onClick = { onSelectOption(SecurityAndPrivacyRoomAccess.Anyone) },
        )
        // Show space member option, but disabled as we don't support this option for now.
        if (saved == SecurityAndPrivacyRoomAccess.SpaceMember) {
            ListItem(
                headlineContent = { Text(text = stringResource(R.string.screen_security_and_privacy_room_access_space_members_option_title)) },
                supportingContent = { Text(text = stringResource(R.string.screen_security_and_privacy_room_access_space_members_option_description)) },
                trailingContent = ListItemContent.RadioButton(selected = true, enabled = false),
                enabled = false,
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
        title = stringResource(R.string.screen_security_and_privacy_room_visibility_section_header),
        modifier = modifier,
    ) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.screen_security_and_privacy_room_visibility_section_footer, homeserverName),
            style = ElementTheme.typography.fontBodyMdRegular,
            color = ElementTheme.colors.textSecondary,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@Composable
private fun RoomAddressSection(
    roomAddress: String?,
    homeserverName: String,
    isVisibleInRoomDirectory: AsyncData<Boolean>,
    onRoomAddressClick: () -> Unit,
    onVisibilityChange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SecurityAndPrivacySection(
        title = stringResource(R.string.screen_security_and_privacy_room_address_section_header),
        modifier = modifier,
    ) {
        ListItem(
            headlineContent = {
                Text(text = roomAddress ?: stringResource(R.string.screen_security_and_privacy_add_room_address_action))
            },
            trailingContent = if (roomAddress.isNullOrEmpty()) ListItemContent.Icon(IconSource.Vector(CompoundIcons.Plus())) else null,
            supportingContent = { Text(text = stringResource(R.string.screen_security_and_privacy_room_address_section_footer)) },
            onClick = onRoomAddressClick,
            colors = ListItemDefaults.colors(trailingIconColor = ElementTheme.colors.iconAccentPrimary),
        )

        ListItem(
            headlineContent = { Text(text = stringResource(R.string.screen_security_and_privacy_room_directory_visibility_toggle_title)) },
            supportingContent = {
                Text(text = stringResource(R.string.screen_security_and_privacy_room_directory_visibility_section_footer, homeserverName))
            },
            onClick = if (isVisibleInRoomDirectory.isSuccess()) onVisibilityChange else null,
            trailingContent = when (isVisibleInRoomDirectory) {
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
                        checked = isVisibleInRoomDirectory.data,
                    )
                }
            }
        )
    }
}

@Composable
private fun EncryptionSection(
    isRoomEncrypted: Boolean,
    canToggleEncryption: Boolean,
    showConfirmation: Boolean,
    onToggleEncryption: () -> Unit,
    onConfirmEncryption: () -> Unit,
    onDismissConfirmation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SecurityAndPrivacySection(
        title = stringResource(R.string.screen_security_and_privacy_encryption_section_header),
        modifier = modifier,
    ) {
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.screen_security_and_privacy_encryption_toggle_title)) },
            supportingContent = { Text(text = stringResource(R.string.screen_security_and_privacy_encryption_section_footer)) },
            trailingContent = ListItemContent.Switch(
                checked = isRoomEncrypted,
                enabled = canToggleEncryption,
            ),
            onClick = if (canToggleEncryption) onToggleEncryption else null
        )
    }
    if (showConfirmation) {
        ConfirmationDialog(
            title = stringResource(R.string.screen_security_and_privacy_enable_encryption_alert_title),
            content = stringResource(R.string.screen_security_and_privacy_enable_encryption_alert_description),
            submitText = stringResource(R.string.screen_security_and_privacy_enable_encryption_alert_confirm_button_title),
            onSubmitClick = onConfirmEncryption,
            onDismiss = onDismissConfirmation,
        )
    }
}

@Composable
private fun HistoryVisibilitySection(
    editedOption: SecurityAndPrivacyHistoryVisibility?,
    savedOptions: SecurityAndPrivacyHistoryVisibility?,
    availableOptions: ImmutableSet<SecurityAndPrivacyHistoryVisibility>,
    onSelectOption: (SecurityAndPrivacyHistoryVisibility) -> Unit,
    modifier: Modifier = Modifier,
) {
    SecurityAndPrivacySection(
        title = stringResource(R.string.screen_security_and_privacy_room_history_section_header),
        modifier = modifier,
    ) {
        for (availableOption in availableOptions) {
            val isSelected = availableOption == editedOption
            HistoryVisibilityItem(
                option = availableOption,
                isSelected = isSelected,
                onSelectOption = onSelectOption,
            )
        }
        // Also show the saved option if it's not in the available options, but disabled
        if (savedOptions != null && !availableOptions.contains(savedOptions)) {
            HistoryVisibilityItem(
                option = savedOptions,
                isSelected = true,
                isEnabled = false,
                onSelectOption = {},
            )
        }
    }
}

@Composable
private fun HistoryVisibilityItem(
    option: SecurityAndPrivacyHistoryVisibility,
    isSelected: Boolean,
    onSelectOption: (SecurityAndPrivacyHistoryVisibility) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
) {
    val headlineText = when (option) {
        SecurityAndPrivacyHistoryVisibility.SinceSelection -> stringResource(R.string.screen_security_and_privacy_room_history_since_selecting_option_title)
        SecurityAndPrivacyHistoryVisibility.SinceInvite -> stringResource(R.string.screen_security_and_privacy_room_history_since_invite_option_title)
        SecurityAndPrivacyHistoryVisibility.Anyone -> stringResource(R.string.screen_security_and_privacy_room_history_anyone_option_title)
    }
    ListItem(
        headlineContent = { Text(text = headlineText) },
        trailingContent = ListItemContent.RadioButton(selected = isSelected, enabled = isEnabled),
        onClick = { onSelectOption(option) },
        enabled = isEnabled,
        modifier = modifier,
    )
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
