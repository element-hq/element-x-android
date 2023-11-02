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

package io.element.android.features.securebackup.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.securebackup.impl.R
import io.element.android.libraries.designsystem.components.async.AsyncLoading
import io.element.android.libraries.designsystem.components.preferences.PreferenceDivider
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.components.preferences.PreferenceText
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.composeutils.annotations.PreviewsDayNight
import io.element.android.libraries.designsystem.text.buildAnnotatedStringWithStyledPart
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarHost
import io.element.android.libraries.designsystem.utils.snackbar.rememberSnackbarHostState
import io.element.android.libraries.matrix.api.encryption.BackupState
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun SecureBackupRootView(
    state: SecureBackupRootState,
    onBackPressed: () -> Unit,
    onSetupClicked: () -> Unit,
    onChangeClicked: () -> Unit,
    onEnableClicked: () -> Unit,
    onDisableClicked: () -> Unit,
    onConfirmRecoveryKeyClicked: () -> Unit,
    onLearnMoreClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = state.snackbarMessage)

    PreferencePage(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = stringResource(id = CommonStrings.common_chat_backup),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        val text = buildAnnotatedStringWithStyledPart(
            fullTextRes = R.string.screen_chat_backup_key_backup_description,
            coloredTextRes = CommonStrings.action_learn_more,
            color = ElementTheme.colors.textPrimary,
            underline = false,
            bold = true,
        )
        PreferenceText(
            title = stringResource(id = R.string.screen_chat_backup_key_backup_title),
            subtitleAnnotated = text,
            onClick = onLearnMoreClicked,
        )

        // Disable / Enable backup
        when (state.backupState) {
            BackupState.UNKNOWN -> Unit
            BackupState.DISABLED -> {
                PreferenceText(
                    title = stringResource(id = R.string.screen_chat_backup_key_backup_action_enable),
                    onClick = onEnableClicked,
                )
            }
            BackupState.CREATING,
            BackupState.ENABLING,
            BackupState.RESUMING,
            BackupState.ENABLED,
            BackupState.DOWNLOADING -> {
                PreferenceText(
                    title = stringResource(id = R.string.screen_chat_backup_key_backup_action_disable),
                    tintColor = ElementTheme.colors.textCriticalPrimary,
                    onClick = onDisableClicked,
                )
            }
            BackupState.DISABLING -> {
                AsyncLoading()
            }
        }

        PreferenceDivider()

        // Setup recovery
        when (state.recoveryState) {
            RecoveryState.UNKNOWN,
            RecoveryState.DISABLED -> {
                PreferenceText(
                    title = stringResource(id = R.string.screen_chat_backup_recovery_action_setup),
                    subtitle = stringResource(id = R.string.screen_chat_backup_recovery_action_setup_description, state.appName),
                    onClick = onSetupClicked,
                    showEndBadge = true,
                )
            }
            RecoveryState.ENABLED -> {
                PreferenceText(
                    title = stringResource(id = R.string.screen_chat_backup_recovery_action_change),
                    onClick = onChangeClicked,
                )
            }
            RecoveryState.INCOMPLETE ->
                PreferenceText(
                    title = stringResource(id = R.string.screen_chat_backup_recovery_action_confirm),
                    subtitle = stringResource(id = R.string.screen_chat_backup_recovery_action_confirm_description),
                    showEndBadge = true,
                    onClick = onConfirmRecoveryKeyClicked,
                )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun SecureBackupRootViewPreview(
    @PreviewParameter(SecureBackupRootStateProvider::class) state: SecureBackupRootState
) = ElementPreview {
    SecureBackupRootView(
        state = state,
        onBackPressed = {},
        onSetupClicked = {},
        onChangeClicked = {},
        onEnableClicked = {},
        onDisableClicked = {},
        onConfirmRecoveryKeyClicked = {},
        onLearnMoreClicked = {},
    )
}
