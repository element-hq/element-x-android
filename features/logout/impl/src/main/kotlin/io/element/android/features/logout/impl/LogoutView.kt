/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.logout.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.components.dialogs.RetryDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * What the user is shown before the device is removed, over the screen they started from.
 * The views only draw dialogs and sheets, so the screen behind them stays visible.
 */
@Composable
fun LogoutView(
    state: LogoutState,
    onDismiss: () -> Unit,
    onGenerateRecoveryKeyClick: (isReplacingExistingKey: Boolean) -> Unit,
    onCheckRecoveryKeyClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    onLearnMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val eventSink = state.eventSink
    val onLogoutClick = { eventSink(LogoutEvent.Logout(ignoreSdkError = false)) }

    Box(modifier = modifier)

    when {
        state.logoutAction.isLoading() -> ProgressDialog(text = stringResource(id = R.string.screen_signout_in_progress_dialog_content))
        state.logoutAction.isFailure() -> RetryDialog(
            title = stringResource(id = CommonStrings.dialog_title_error),
            content = stringResource(id = CommonStrings.error_unknown),
            retryText = stringResource(id = CommonStrings.action_signout_anyway),
            onRetry = { eventSink(LogoutEvent.Logout(ignoreSdkError = true)) },
            onDismiss = { eventSink(LogoutEvent.CloseDialogs) },
        )
        else -> when (state.confirmation) {
            LogoutConfirmation.Loading -> ProgressDialog(
                showCancelButton = true,
                onDismissRequest = onDismiss,
            )
            LogoutConfirmation.WaitingForBackup -> ProgressDialog(
                text = stringResource(id = R.string.screen_signout_in_progress_dialog_content),
                showCancelButton = true,
                onDismissRequest = onDismiss,
            )
            LogoutConfirmation.OtherVerifiedDevice -> ConfirmationDialog(
                title = stringResource(id = R.string.screen_signout_other_verified_device_dialog_title),
                content = stringResource(id = R.string.screen_signout_other_verified_device_dialog_content),
                submitText = stringResource(id = CommonStrings.action_remove),
                destructiveSubmit = true,
                onSubmitClick = onLogoutClick,
                onDismiss = onDismiss,
            )
            LogoutConfirmation.KeyBackupDisabled -> ConfirmationDialog(
                title = stringResource(id = R.string.screen_signout_key_backup_disabled_title),
                content = stringResource(id = R.string.screen_signout_key_backup_disabled_subtitle),
                submitText = stringResource(id = CommonStrings.action_remove),
                cancelText = stringResource(id = CommonStrings.common_settings),
                destructiveSubmit = true,
                onSubmitClick = onLogoutClick,
                onCancelClick = onOpenSettingsClick,
                onDismiss = onDismiss,
            )
            LogoutConfirmation.BackupOngoing -> ErrorDialog(
                title = stringResource(id = R.string.screen_signout_key_backup_ongoing_title),
                content = stringResource(id = R.string.screen_signout_key_backup_ongoing_subtitle),
                onSubmit = onDismiss,
            )
            LogoutConfirmation.BackupOffline -> ErrorDialog(
                title = stringResource(id = R.string.screen_signout_key_backup_offline_title),
                content = stringResource(id = R.string.screen_signout_key_backup_offline_subtitle),
                onSubmit = onDismiss,
            )
            LogoutConfirmation.RecoveryKeyOnly -> RecoveryKeyOnlySheet(
                onDismiss = onDismiss,
                onLogoutClick = onLogoutClick,
                onCheckRecoveryKeyClick = onCheckRecoveryKeyClick,
                onGenerateRecoveryKeyClick = { onGenerateRecoveryKeyClick(true) },
            )
            LogoutConfirmation.NoRecovery -> NoRecoverySheet(
                onDismiss = onDismiss,
                onLogoutClick = onLogoutClick,
                onGenerateRecoveryKeyClick = { onGenerateRecoveryKeyClick(false) },
                onLearnMoreClick = onLearnMoreClick,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecoveryKeyOnlySheet(
    onDismiss: () -> Unit,
    onLogoutClick: () -> Unit,
    onCheckRecoveryKeyClick: () -> Unit,
    onGenerateRecoveryKeyClick: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        scrollable = true,
        // Skip the partially expanded state, which would hide the buttons below the fold.
        sheetState = rememberBottomSheetState(
            initialValue = SheetValue.Hidden,
            enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
        ),
    ) {
        IconTitleSubtitleMolecule(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            title = stringResource(id = R.string.screen_signout_save_recovery_key_title),
            subTitle = stringResource(id = R.string.screen_signout_save_recovery_key_subtitle),
            iconStyle = BigIcon.Style.Default(CompoundIcons.InfoSolid()),
        )
        ButtonColumnMolecule(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Button(
                text = stringResource(id = R.string.screen_signout_save_recovery_key_action_continue),
                destructive = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.signOut),
                onClick = onLogoutClick,
            )
            OutlinedButton(
                text = stringResource(id = R.string.screen_signout_save_recovery_key_action_check),
                modifier = Modifier.fillMaxWidth(),
                onClick = onCheckRecoveryKeyClick,
            )
            TextButton(
                text = stringResource(id = R.string.screen_signout_save_recovery_key_action_generate),
                modifier = Modifier.fillMaxWidth(),
                onClick = onGenerateRecoveryKeyClick,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoRecoverySheet(
    onDismiss: () -> Unit,
    onLogoutClick: () -> Unit,
    onGenerateRecoveryKeyClick: () -> Unit,
    onLearnMoreClick: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        scrollable = true,
        // Skip the partially expanded state, which would hide the buttons below the fold.
        sheetState = rememberBottomSheetState(
            initialValue = SheetValue.Hidden,
            enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
        ),
    ) {
        IconTitleSubtitleMolecule(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            title = stringResource(id = R.string.screen_signout_recovery_disabled_title),
            subTitle = stringResource(id = R.string.screen_signout_recovery_disabled_subtitle),
            iconStyle = BigIcon.Style.AlertSolid,
        )
        TextButton(
            text = stringResource(id = CommonStrings.action_learn_more),
            modifier = Modifier.fillMaxWidth(),
            onClick = onLearnMoreClick,
        )
        ButtonColumnMolecule(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Button(
                text = stringResource(id = R.string.screen_chat_backup_recovery_action_setup),
                leadingIcon = IconSource.Vector(CompoundIcons.Key()),
                modifier = Modifier.fillMaxWidth(),
                onClick = onGenerateRecoveryKeyClick,
            )
            TextButton(
                text = stringResource(id = CommonStrings.action_signout_anyway),
                destructive = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.signOut),
                onClick = onLogoutClick,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun LogoutViewPreview(
    @PreviewParameter(LogoutStatePreviewParam::class) state: LogoutState,
) = ElementPreview {
    LogoutView(
        state = state,
        onDismiss = {},
        onGenerateRecoveryKeyClick = {},
        onCheckRecoveryKeyClick = {},
        onOpenSettingsClick = {},
        onLearnMoreClick = {},
    )
}
