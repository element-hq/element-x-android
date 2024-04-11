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

package io.element.android.features.securebackup.impl.disable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.securebackup.impl.R
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun SecureBackupDisableView(
    state: SecureBackupDisableState,
    onDone: () -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowStepPage(
        modifier = modifier,
        onBackClicked = onBackClicked,
        title = stringResource(id = R.string.screen_key_backup_disable_title),
        subTitle = stringResource(id = R.string.screen_key_backup_disable_description),
        iconStyle = BigIcon.Style.Default(CompoundIcons.KeyOffSolid()),
        content = { Content(state = state) },
        buttons = { Buttons(state = state) },
    )

    AsyncActionView(
        async = state.disableAction,
        confirmationDialog = {
            SecureBackupDisableConfirmationDialog(
                onConfirm = { state.eventSink.invoke(SecureBackupDisableEvents.DisableBackup) },
                onDismiss = { state.eventSink.invoke(SecureBackupDisableEvents.DismissDialogs) },
            )
        },
        progressDialog = {},
        errorMessage = { it.message ?: it.toString() },
        onErrorDismiss = { state.eventSink.invoke(SecureBackupDisableEvents.DismissDialogs) },
        onSuccess = { onDone() },
    )
}

@Composable
private fun SecureBackupDisableConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    ConfirmationDialog(
        title = stringResource(id = R.string.screen_key_backup_disable_confirmation_title),
        content = stringResource(id = R.string.screen_key_backup_disable_confirmation_description),
        submitText = stringResource(id = R.string.screen_key_backup_disable_confirmation_action_turn_off),
        destructiveSubmit = true,
        onSubmitClicked = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
private fun ColumnScope.Buttons(
    state: SecureBackupDisableState,
) {
    Button(
        text = stringResource(id = R.string.screen_chat_backup_key_backup_action_disable),
        showProgress = state.disableAction.isLoading(),
        destructive = true,
        modifier = Modifier.fillMaxWidth(),
        onClick = { state.eventSink.invoke(SecureBackupDisableEvents.DisableBackup) }
    )
}

@Composable
private fun Content(state: SecureBackupDisableState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SecureBackupDisableItem(stringResource(id = R.string.screen_key_backup_disable_description_point_1))
        SecureBackupDisableItem(stringResource(id = R.string.screen_key_backup_disable_description_point_2, state.appName))
    }
}

@Composable
private fun SecureBackupDisableItem(text: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Icon(
            imageVector = CompoundIcons.Close(),
            contentDescription = null,
            tint = ElementTheme.colors.iconCriticalPrimary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            modifier = Modifier.padding(start = 8.dp, end = 4.dp),
            text = text,
            color = ElementTheme.colors.textSecondary,
            style = ElementTheme.typography.fontBodyMdRegular,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun SecureBackupDisableViewPreview(
    @PreviewParameter(SecureBackupDisableStateProvider::class) state: SecureBackupDisableState
) = ElementPreview {
    SecureBackupDisableView(
        state = state,
        onDone = {},
        onBackClicked = {},
    )
}
