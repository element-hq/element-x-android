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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.securebackup.impl.R
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.async.AsyncLoading
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.preferences.PreferenceDivider
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.components.preferences.PreferenceText
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.buildAnnotatedStringWithStyledPart
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarHost
import io.element.android.libraries.designsystem.utils.snackbar.rememberSnackbarHostState
import io.element.android.libraries.matrix.api.encryption.BackupState
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun SecureBackupRootView(
    state: SecureBackupRootState,
    onBackClick: () -> Unit,
    onSetupClick: () -> Unit,
    onChangeClick: () -> Unit,
    onEnableClick: () -> Unit,
    onDisableClick: () -> Unit,
    onConfirmRecoveryKeyClick: () -> Unit,
    onLearnMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = state.snackbarMessage)

    PreferencePage(
        modifier = modifier,
        onBackClick = onBackClick,
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
            onClick = onLearnMoreClick,
        )

        // Disable / Enable backup
        when (state.backupState) {
            BackupState.WAITING_FOR_SYNC -> Unit
            BackupState.UNKNOWN -> {
                when (state.doesBackupExistOnServer) {
                    is AsyncData.Success -> when (state.doesBackupExistOnServer.data) {
                        true -> {
                            PreferenceText(
                                title = stringResource(id = R.string.screen_chat_backup_key_backup_action_disable),
                                tintColor = ElementTheme.colors.textCriticalPrimary,
                                onClick = onDisableClick,
                            )
                        }
                        false -> {
                            PreferenceText(
                                title = stringResource(id = R.string.screen_chat_backup_key_backup_action_enable),
                                onClick = onEnableClick,
                            )
                        }
                    }
                    is AsyncData.Loading,
                    AsyncData.Uninitialized -> {
                        ListItem(headlineContent = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        })
                    }
                    is AsyncData.Failure -> {
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = stringResource(id = CommonStrings.error_unknown),
                                )
                            },
                            trailingContent = ListItemContent.Custom {
                                TextButton(
                                    text = stringResource(
                                        id = CommonStrings.action_retry
                                    ),
                                    onClick = { state.eventSink.invoke(SecureBackupRootEvents.RetryKeyBackupState) }
                                )
                            }
                        )

                        PreferenceText(
                            title = stringResource(id = R.string.screen_chat_backup_key_backup_action_enable),
                            onClick = onEnableClick,
                        )
                    }
                }
            }
            BackupState.CREATING,
            BackupState.ENABLING,
            BackupState.RESUMING,
            BackupState.ENABLED,
            BackupState.DOWNLOADING -> {
                PreferenceText(
                    title = stringResource(id = R.string.screen_chat_backup_key_backup_action_disable),
                    tintColor = ElementTheme.colors.textCriticalPrimary,
                    onClick = onDisableClick,
                )
            }
            BackupState.DISABLING -> {
                AsyncLoading()
            }
        }

        PreferenceDivider()

        // Setup recovery
        when (state.recoveryState) {
            RecoveryState.WAITING_FOR_SYNC -> Unit
            RecoveryState.UNKNOWN,
            RecoveryState.DISABLED -> {
                PreferenceText(
                    title = stringResource(id = R.string.screen_chat_backup_recovery_action_setup),
                    subtitle = stringResource(id = R.string.screen_chat_backup_recovery_action_setup_description, state.appName),
                    onClick = onSetupClick,
                    showEndBadge = true,
                )
            }
            RecoveryState.ENABLED -> {
                PreferenceText(
                    title = stringResource(id = R.string.screen_chat_backup_recovery_action_change),
                    onClick = onChangeClick,
                )
            }
            RecoveryState.INCOMPLETE ->
                PreferenceText(
                    title = stringResource(id = R.string.screen_chat_backup_recovery_action_confirm),
                    subtitle = stringResource(id = R.string.screen_chat_backup_recovery_action_confirm_description),
                    showEndBadge = true,
                    onClick = onConfirmRecoveryKeyClick,
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
        onBackClick = {},
        onSetupClick = {},
        onChangeClick = {},
        onEnableClick = {},
        onDisableClick = {},
        onConfirmRecoveryKeyClick = {},
        onLearnMoreClick = {},
    )
}
