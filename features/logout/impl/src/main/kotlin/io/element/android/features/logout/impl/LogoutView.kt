/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
import io.element.android.libraries.designsystem.components.BigIcon
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
    onChangeRecoveryKeyClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val eventSink = state.eventSink

    FlowStepPage(
        onBackClick = onBackClick,
        title = title(state),
        subTitle = subtitle(state),
        iconStyle = BigIcon.Style.Default(CompoundIcons.KeySolid()),
        modifier = modifier,
        buttons = {
            Buttons(
                state = state,
                onChangeRecoveryKeyClick = onChangeRecoveryKeyClick,
                onLogoutClick = {
                    eventSink(LogoutEvents.Logout(ignoreSdkError = false))
                }
            )
        },
    ) {
        Content(state)
    }

    LogoutActionDialog(
        state.logoutAction,
        onConfirmClick = {
            eventSink(LogoutEvents.Logout(ignoreSdkError = false))
        },
        onForceLogoutClick = {
            eventSink(LogoutEvents.Logout(ignoreSdkError = true))
        },
        onDismissDialog = {
            eventSink(LogoutEvents.CloseDialogs)
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
    onLogoutClick: () -> Unit,
    onChangeRecoveryKeyClick: () -> Unit,
) {
    val logoutAction = state.logoutAction
    if (state.isLastDevice) {
        OutlinedButton(
            text = stringResource(id = CommonStrings.common_settings),
            modifier = Modifier.fillMaxWidth(),
            onClick = onChangeRecoveryKeyClick,
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
        onClick = onLogoutClick,
    )
}

@Composable
private fun Content(
    state: LogoutState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 60.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (state.backupUploadState) {
            is BackupUploadState.Uploading -> {
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
            BackupUploadState.Waiting -> {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    trackColor = ElementTheme.colors.progressIndicatorTrackColor,
                )
                if (state.waitingForALongTime) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = stringResource(CommonStrings.common_please_check_internet_connection),
                        style = ElementTheme.typography.fontBodySmRegular,
                    )
                }
            }
            else -> Unit
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
        onChangeRecoveryKeyClick = {},
        onBackClick = {},
    )
}
