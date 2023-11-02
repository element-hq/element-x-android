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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.securebackup.impl.R
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.composeutils.annotations.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.theme.ElementTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecureBackupDisableView(
    state: SecureBackupDisableState,
    onDone: () -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(state.disableAction) {
        if (state.disableAction is Async.Success) {
            onDone()
        }
    }
    HeaderFooterPage(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton(onClick = onBackClicked) },
                title = {},
            )
        },
        header = {
            HeaderContent()
        },
        footer = {
            BottomMenu(state = state)
        }
    ) {
        Content(state = state)
    }
    if (state.showConfirmationDialog) {
        SecureBackupDisableConfirmationDialog(
            onConfirm = { state.eventSink.invoke(SecureBackupDisableEvents.DisableBackup(force = true)) },
            onDismiss = { state.eventSink.invoke(SecureBackupDisableEvents.DismissDialogs) },
        )
    } else if (state.disableAction is Async.Failure) {
        ErrorDialog(
            content = state.disableAction.error.let { it.message ?: it.toString() },
            onDismiss = { state.eventSink.invoke(SecureBackupDisableEvents.DismissDialogs) },
        )
    }
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
private fun HeaderContent(
    modifier: Modifier = Modifier,
) {
    IconTitleSubtitleMolecule(
        modifier = modifier.padding(top = 0.dp),
        iconResourceId = CommonDrawables.ic_key_off,
        title = stringResource(id = R.string.screen_key_backup_disable_title),
        subTitle = stringResource(id = R.string.screen_key_backup_disable_description),
    )
}

@Composable
private fun BottomMenu(
    state: SecureBackupDisableState,
) {
    ButtonColumnMolecule(
        modifier = Modifier.padding(bottom = 20.dp)
    ) {
        Button(
            text = stringResource(id = R.string.screen_chat_backup_key_backup_action_disable),
            showProgress = state.disableAction.isLoading(),
            destructive = true,
            modifier = Modifier.fillMaxWidth(),
            onClick = { state.eventSink.invoke(SecureBackupDisableEvents.DisableBackup(force = false)) }
        )
    }
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
            resourceId = CommonDrawables.ic_compound_close,
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
