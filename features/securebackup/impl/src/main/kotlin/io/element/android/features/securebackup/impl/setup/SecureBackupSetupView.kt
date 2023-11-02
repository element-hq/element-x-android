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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.securebackup.impl.R
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyView
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyViewState
import io.element.android.libraries.androidutils.system.copyToClipboard
import io.element.android.libraries.androidutils.system.startSharePlainTextIntent
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.composeutils.annotations.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecureBackupSetupView(
    state: SecureBackupSetupState,
    onDone: () -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val canGoBack = state.canGoBack()
    BackHandler(enabled = canGoBack) {
        onBackClicked()
    }
    HeaderFooterPage(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (canGoBack) {
                        BackButton(onClick = onBackClicked)
                    }
                },
                title = {},
            )
        },
        header = {
            HeaderContent(state = state)
        },
        footer = {
            val chooserTitle = stringResource(id = R.string.screen_recovery_key_save_action)
            BottomMenu(
                state = state,
                onSaveClicked = { key ->
                    context.startSharePlainTextIntent(
                        activityResultLauncher = null,
                        chooserTitle = chooserTitle,
                        text = key,
                    )
                    state.eventSink.invoke(SecureBackupSetupEvents.RecoveryKeyHasBeenSaved)
                },
                onDone = {
                    if (state.setupState is SetupState.CreatedAndSaved) {
                        onDone()
                    } else {
                        state.eventSink.invoke(SecureBackupSetupEvents.Done)
                    }
                },
            )
        }
    ) {
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
        Content(state = state.recoveryKeyViewState, onClick = clickLambda)
    }

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
private fun HeaderContent(
    state: SecureBackupSetupState,
    modifier: Modifier = Modifier,
) {
    val setupState = state.setupState
    val title = when (setupState) {
        SetupState.Init,
        SetupState.Creating -> if (state.isChangeRecoveryKeyUserStory)
            stringResource(id = R.string.screen_recovery_key_change_title)
        else
            stringResource(id = R.string.screen_recovery_key_setup_title)
        is SetupState.Created,
        is SetupState.CreatedAndSaved ->
            stringResource(id = R.string.screen_recovery_key_save_title)
    }
    val subTitle = when (setupState) {
        SetupState.Init,
        SetupState.Creating -> if (state.isChangeRecoveryKeyUserStory)
            stringResource(id = R.string.screen_recovery_key_change_description)
        else
            stringResource(id = R.string.screen_recovery_key_setup_description)
        is SetupState.Created,
        is SetupState.CreatedAndSaved ->
            stringResource(id = R.string.screen_recovery_key_save_description)
    }
    IconTitleSubtitleMolecule(
        modifier = modifier.padding(top = 0.dp),
        iconResourceId = CommonDrawables.ic_key,
        title = title,
        subTitle = subTitle,
    )
}

@Composable
private fun BottomMenu(
    state: SecureBackupSetupState,
    onSaveClicked: (String) -> Unit,
    onDone: () -> Unit,
) {
    val setupState = state.setupState
    ButtonColumnMolecule(
        modifier = Modifier.padding(bottom = 20.dp)
    ) {
        when (setupState) {
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
                    leadingIcon = IconSource.Resource(CommonDrawables.ic_compound_download),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSaveClicked(setupState.recoveryKey()!!) },
                )
                Button(
                    text = stringResource(id = CommonStrings.action_done),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDone,
                )
            }
        }
    }
}

@Composable
private fun Content(
    state: RecoveryKeyViewState,
    onClick: (() -> Unit)?,
) {
    val modifier = Modifier.padding(top = 52.dp)
    RecoveryKeyView(
        modifier = modifier,
        state = state,
        onClick = onClick,
        onChange = null,
        onSubmit = null,
    )
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
