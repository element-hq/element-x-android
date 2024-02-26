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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.logout.impl.tools.isBackingUp
import io.element.android.features.logout.impl.ui.LogoutActionDialog
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.LinearProgressIndicator
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.progressIndicatorTrackColor
import io.element.android.libraries.matrix.api.encryption.BackupState
import io.element.android.libraries.matrix.api.encryption.BackupUploadState
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.encryption.SteadyStateException
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun LogoutView(
    state: LogoutState,
    onChangeRecoveryKeyClicked: () -> Unit,
    onBackClicked: () -> Unit,
    onSuccessLogout: (logoutUrlResult: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val eventSink = state.eventSink

    FlowStepPage(
        onBackClicked = onBackClicked,
        title = title(state),
        subTitle = subtitle(state),
        iconVector = CompoundIcons.KeySolid(),
        modifier = modifier,
        content = { Content(state) },
        buttons = {
            Buttons(
                state = state,
                onChangeRecoveryKeyClicked = onChangeRecoveryKeyClicked,
                onLogoutClicked = {
                    eventSink(LogoutEvents.Logout(ignoreSdkError = false))
                }
            )
        },
    )

    LogoutActionDialog(
        state.logoutAction,
        onConfirmClicked = {
            eventSink(LogoutEvents.Logout(ignoreSdkError = false))
        },
        onForceLogoutClicked = {
            eventSink(LogoutEvents.Logout(ignoreSdkError = true))
        },
        onDismissDialog = {
            eventSink(LogoutEvents.CloseDialogs)
        },
        onSuccessLogout = {
            onSuccessLogout(it)
        },
    )
}

@Composable
private fun title(state: LogoutState): String {
    return when {
        state.backupUploadState.isBackingUp() -> stringResource(id = R.string.screen_signout_key_backup_ongoing_title)
        state.isLastDevice -> {
            if (state.recoveryState != RecoveryState.ENABLED) {
                stringResource(id = R.string.screen_signout_recovery_disabled_title)
            } else if (state.backupState == BackupState.UNKNOWN && state.doesBackupExistOnServer.not()) {
                stringResource(id = R.string.screen_signout_key_backup_disabled_title)
            } else {
                stringResource(id = R.string.screen_signout_save_recovery_key_title)
            }
        }
        else -> stringResource(CommonStrings.action_signout)
    }
}

@Composable
private fun subtitle(state: LogoutState): String? {
    return when {
        (state.backupUploadState as? BackupUploadState.SteadyException)?.exception is SteadyStateException.Connection ->
            stringResource(id = R.string.screen_signout_key_backup_offline_subtitle)
        state.backupUploadState.isBackingUp() -> stringResource(id = R.string.screen_signout_key_backup_ongoing_subtitle)
        state.isLastDevice -> stringResource(id = R.string.screen_signout_key_backup_disabled_subtitle)
        else -> null
    }
}

@Composable
private fun ColumnScope.Buttons(
    state: LogoutState,
    onLogoutClicked: () -> Unit,
    onChangeRecoveryKeyClicked: () -> Unit,
) {
    val logoutAction = state.logoutAction
    if (state.isLastDevice) {
        OutlinedButton(
            text = stringResource(id = CommonStrings.common_settings),
            modifier = Modifier.fillMaxWidth(),
            onClick = onChangeRecoveryKeyClicked,
        )
    }
    val signOutSubmitRes = when {
        logoutAction is AsyncAction.Loading -> R.string.screen_signout_in_progress_dialog_content
        state.backupUploadState.isBackingUp() -> CommonStrings.action_signout_anyway
        else -> CommonStrings.action_signout
    }
    Button(
        text = stringResource(id = signOutSubmitRes),
        showProgress = logoutAction is AsyncAction.Loading,
        destructive = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TestTags.signOut),
        onClick = onLogoutClicked,
    )
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
                progress = { state.backupUploadState.backedUpCount.toFloat() / state.backupUploadState.totalCount.toFloat() },
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
