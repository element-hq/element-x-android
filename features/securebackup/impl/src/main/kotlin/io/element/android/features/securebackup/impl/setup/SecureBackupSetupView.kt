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

package io.element.android.features.securebackup.impl.setup

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.securebackup.impl.R
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyView
import io.element.android.libraries.androidutils.system.copyToClipboard
import io.element.android.libraries.androidutils.system.startSharePlainTextIntent
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun SecureBackupSetupView(
    state: SecureBackupSetupState,
    onDone: () -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowStepPage(
        modifier = modifier,
        onBackClicked = onBackClicked.takeIf { state.canGoBack() },
        title = title(state),
        subTitle = subtitle(state),
        iconVector = CompoundIcons.KeySolid(),
        content = { Content(state) },
        buttons = { Buttons(state, onDone = onDone) },
    )

    if (state.showSaveConfirmationDialog) {
        ConfirmationDialog(
            title = stringResource(id = R.string.screen_recovery_key_setup_confirmation_title),
            content = stringResource(id = R.string.screen_recovery_key_setup_confirmation_description),
            submitText = stringResource(id = CommonStrings.action_continue),
            onSubmitClicked = onDone,
            onDismiss = {
                state.eventSink.invoke(SecureBackupSetupEvents.DismissDialog)
            }
        )
    }
}

private fun SecureBackupSetupState.canGoBack(): Boolean {
    return recoveryKeyViewState.formattedRecoveryKey == null
}

@Composable
private fun title(state: SecureBackupSetupState): String {
    return when (state.setupState) {
        SetupState.Init,
        SetupState.Creating -> if (state.isChangeRecoveryKeyUserStory) {
            stringResource(id = R.string.screen_recovery_key_change_title)
        } else {
            stringResource(id = R.string.screen_recovery_key_setup_title)
        }
        is SetupState.Created,
        is SetupState.CreatedAndSaved ->
            stringResource(id = R.string.screen_recovery_key_save_title)
    }
}

@Composable
private fun subtitle(state: SecureBackupSetupState): String {
    return when (state.setupState) {
        SetupState.Init,
        SetupState.Creating -> if (state.isChangeRecoveryKeyUserStory) {
            stringResource(id = R.string.screen_recovery_key_change_description)
        } else {
            stringResource(id = R.string.screen_recovery_key_setup_description)
        }
        is SetupState.Created,
        is SetupState.CreatedAndSaved ->
            stringResource(id = R.string.screen_recovery_key_save_description)
    }
}

@Composable
private fun Content(
    state: SecureBackupSetupState,
) {
    val context = LocalContext.current
    val formattedRecoveryKey = state.recoveryKeyViewState.formattedRecoveryKey
    val clickLambda = if (formattedRecoveryKey != null) {
        {
            context.copyToClipboard(
                formattedRecoveryKey,
                context.getString(R.string.screen_recovery_key_copied_to_clipboard)
            )
            state.eventSink.invoke(SecureBackupSetupEvents.RecoveryKeyHasBeenSaved)
        }
    } else {
        if (!state.recoveryKeyViewState.inProgress) {
            {
                state.eventSink.invoke(SecureBackupSetupEvents.CreateRecoveryKey)
            }
        } else {
            null
        }
    }
    RecoveryKeyView(
        modifier = Modifier.padding(top = 52.dp),
        state = state.recoveryKeyViewState,
        onClick = clickLambda,
        onChange = null,
        onSubmit = null,
    )
}

@Composable
private fun ColumnScope.Buttons(
    state: SecureBackupSetupState,
    onDone: () -> Unit,
) {
    val context = LocalContext.current
    val chooserTitle = stringResource(id = R.string.screen_recovery_key_save_action)
    when (state.setupState) {
        SetupState.Init,
        SetupState.Creating -> {
            Button(
                text = stringResource(id = CommonStrings.action_done),
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                onClick = onDone
            )
        }
        is SetupState.Created,
        is SetupState.CreatedAndSaved -> {
            OutlinedButton(
                text = stringResource(id = R.string.screen_recovery_key_save_action),
                leadingIcon = IconSource.Vector(CompoundIcons.Download()),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    context.startSharePlainTextIntent(
                        activityResultLauncher = null,
                        chooserTitle = chooserTitle,
                        text = state.setupState.recoveryKey()!!,
                    )
                    state.eventSink.invoke(SecureBackupSetupEvents.RecoveryKeyHasBeenSaved)
                },
            )
            Button(
                text = stringResource(id = CommonStrings.action_done),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (state.setupState is SetupState.CreatedAndSaved) {
                        onDone()
                    } else {
                        state.eventSink.invoke(SecureBackupSetupEvents.Done)
                    }
                },
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun SecureBackupSetupViewPreview(
    @PreviewParameter(SecureBackupSetupStateProvider::class) state: SecureBackupSetupState
) = ElementPreview {
    SecureBackupSetupView(
        state = state,
        onDone = {},
        onBackClicked = {},
    )
}
