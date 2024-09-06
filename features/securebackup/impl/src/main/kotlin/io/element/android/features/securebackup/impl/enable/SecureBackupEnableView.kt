/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.enable

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.securebackup.impl.R
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button

@Composable
fun SecureBackupEnableView(
    state: SecureBackupEnableState,
    onSuccess: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowStepPage(
        modifier = modifier,
        onBackClick = onBackClick,
        title = stringResource(id = R.string.screen_chat_backup_key_backup_action_enable),
        iconStyle = BigIcon.Style.Default(CompoundIcons.KeySolid()),
        buttons = { Buttons(state = state) }
    )
    AsyncActionView(
        async = state.enableAction,
        progressDialog = { },
        onSuccess = { onSuccess() },
        onErrorDismiss = { state.eventSink.invoke(SecureBackupEnableEvents.DismissDialog) }
    )
}

@Composable
private fun ColumnScope.Buttons(
    state: SecureBackupEnableState,
) {
    Button(
        text = stringResource(id = R.string.screen_chat_backup_key_backup_action_enable),
        showProgress = state.enableAction.isLoading(),
        modifier = Modifier.fillMaxWidth(),
        onClick = { state.eventSink.invoke(SecureBackupEnableEvents.EnableBackup) }
    )
}

@PreviewsDayNight
@Composable
internal fun SecureBackupEnableViewPreview(
    @PreviewParameter(SecureBackupEnableStateProvider::class) state: SecureBackupEnableState
) = ElementPreview {
    SecureBackupEnableView(
        state = state,
        onSuccess = {},
        onBackClick = {},
    )
}
