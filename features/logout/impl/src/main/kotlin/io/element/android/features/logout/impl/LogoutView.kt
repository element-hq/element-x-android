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

package io.element.android.features.logout.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.LinearProgressIndicator
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.theme.progressIndicatorTrackColor
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.matrix.api.encryption.BackupUploadState
import io.element.android.libraries.matrix.api.encryption.SteadyStateException
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogoutView(
    state: LogoutState,
    onChangeRecoveryKeyClicked: () -> Unit,
    onBackClicked: () -> Unit,
    onSuccessLogout: (logoutUrlResult: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val eventSink = state.eventSink

    HeaderFooterPage(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton(onClick = onBackClicked) },
                title = {},
            )
        },
        header = {
            HeaderContent(state = state)
        },
        footer = {
            BottomMenu(
                state = state,
                onChangeRecoveryKeyClicked = onChangeRecoveryKeyClicked,
                onLogoutClicked = {
                    eventSink(LogoutEvents.Logout(ignoreSdkError = false))
                },
            )
        }
    ) {
        Content(state = state)
    }

    // Log out confirmation dialog
    if (state.showConfirmationDialog) {
        ConfirmationDialog(
            title = stringResource(id = CommonStrings.action_signout),
            content = stringResource(id = R.string.screen_signout_confirmation_dialog_content),
            submitText = stringResource(id = CommonStrings.action_signout),
            onCancelClicked = {
                eventSink(LogoutEvents.CloseDialogs)
            },
            onSubmitClicked = {
                eventSink(LogoutEvents.Logout(ignoreSdkError = false))
            },
            onDismiss = {
                eventSink(LogoutEvents.CloseDialogs)
            }
        )
    }

    when (state.logoutAction) {
        is Async.Loading ->
            ProgressDialog(text = stringResource(id = R.string.screen_signout_in_progress_dialog_content))
        is Async.Failure ->
            ConfirmationDialog(
                title = stringResource(id = CommonStrings.dialog_title_error),
                content = stringResource(id = CommonStrings.error_unknown),
                submitText = stringResource(id = CommonStrings.action_signout_anyway),
                onCancelClicked = {
                    eventSink(LogoutEvents.CloseDialogs)
                },
                onSubmitClicked = {
                    eventSink(LogoutEvents.Logout(ignoreSdkError = true))
                },
                onDismiss = {
                    eventSink(LogoutEvents.CloseDialogs)
                }
            )
        Async.Uninitialized ->
            Unit
        is Async.Success ->
            LaunchedEffect(state.logoutAction) {
                onSuccessLogout(state.logoutAction.data)
            }
    }
}

@Composable
private fun HeaderContent(
    state: LogoutState,
    modifier: Modifier = Modifier,
) {
    val title = when {
        state.backupUploadState.isBackingUp() -> stringResource(id = R.string.screen_signout_key_backup_ongoing_title)
        state.isLastSession -> stringResource(id = R.string.screen_signout_key_backup_disabled_title)
        else -> stringResource(CommonStrings.action_signout)
    }
    val subtitle = when {
        (state.backupUploadState as? BackupUploadState.SteadyException)?.exception is SteadyStateException.Connection ->
            stringResource(id = R.string.screen_signout_key_backup_offline_subtitle)
        state.backupUploadState.isBackingUp() -> stringResource(id = R.string.screen_signout_key_backup_ongoing_subtitle)
        state.isLastSession -> stringResource(id = R.string.screen_signout_key_backup_disabled_subtitle)
        else -> null
    }

    val paddingTop = 0.dp
    IconTitleSubtitleMolecule(
        modifier = modifier.padding(top = paddingTop),
        iconResourceId = CommonDrawables.ic_key,
        title = title,
        subTitle = subtitle,
    )
}

private fun BackupUploadState.isBackingUp(): Boolean {
    return when (this) {
        BackupUploadState.Unknown,
        BackupUploadState.Waiting,
        is BackupUploadState.Uploading,
        is BackupUploadState.CheckingIfUploadNeeded -> true
        is BackupUploadState.SteadyException -> exception is SteadyStateException.Connection
        BackupUploadState.Done,
        BackupUploadState.Error -> false
    }
}

@Composable
private fun BottomMenu(
    state: LogoutState,
    onLogoutClicked: () -> Unit,
    onChangeRecoveryKeyClicked: () -> Unit,
) {
    val logoutAction = state.logoutAction
    ButtonColumnMolecule(
        modifier = Modifier.padding(bottom = 20.dp)
    ) {
        if (state.isLastSession) {
            OutlinedButton(
                text = stringResource(id = CommonStrings.common_settings),
                modifier = Modifier.fillMaxWidth(),
                onClick = onChangeRecoveryKeyClicked,
            )
        }
        val signOutSubmitRes = when {
            logoutAction is Async.Loading -> R.string.screen_signout_in_progress_dialog_content
            state.backupUploadState.isBackingUp() -> CommonStrings.action_signout_anyway
            else -> CommonStrings.action_signout
        }
        Button(
            text = stringResource(id = signOutSubmitRes),
            showProgress = logoutAction is Async.Loading,
            destructive = true,
            modifier = Modifier.fillMaxWidth(),
            onClick = onLogoutClicked,
        )
    }
}

@Composable
private fun Content(
    state: LogoutState,
) {
    if (state.backupUploadState is BackupUploadState.Uploading) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 60.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                progress = state.backupUploadState.backedUpCount.toFloat() / state.backupUploadState.totalCount.toFloat(),
                trackColor = ElementTheme.colors.progressIndicatorTrackColor,
            )
            Text(
                modifier = Modifier.align(Alignment.End),
                text = "${state.backupUploadState.backedUpCount} / ${state.backupUploadState.totalCount}",
                style = ElementTheme.typography.fontBodySmRegular,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun LogoutViewPreview(
    @PreviewParameter(LogoutStateProvider::class) state: LogoutState,
) = ElementPreview {
    LogoutView(
        state,
        onChangeRecoveryKeyClicked = {},
        onSuccessLogout = {},
        onBackClicked = {},
    )
}
