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

package io.element.android.features.securebackup.impl.enter

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.securebackup.impl.R
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyView
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecureBackupEnterRecoveryKeyView(
    state: SecureBackupEnterRecoveryKeyState,
    onDone: () -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state.submitAction) {
        Async.Uninitialized -> Unit
        is Async.Failure -> ErrorDialog(
            content = state.submitAction.error.message ?: state.submitAction.error.toString(),
            onDismiss = {
                state.eventSink(SecureBackupEnterRecoveryKeyEvents.ClearDialog)
            }
        )
        is Async.Loading -> Unit
        is Async.Success -> LaunchedEffect(state.submitAction) {
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
            BottomMenu(
                state = state,
                onSubmit = {
                    state.eventSink.invoke(SecureBackupEnterRecoveryKeyEvents.Submit)
                },
            )
        }
    ) {
        Content(
            state = state,
            onChange = {
                state.eventSink.invoke(SecureBackupEnterRecoveryKeyEvents.OnRecoveryKeyChange(it))
            },
            onSubmit = {
                state.eventSink.invoke(SecureBackupEnterRecoveryKeyEvents.Submit)
            })
    }
}

@Composable
private fun HeaderContent(
    modifier: Modifier = Modifier,
) {
    IconTitleSubtitleMolecule(
        modifier = modifier.padding(top = 0.dp),
        iconResourceId = CommonDrawables.ic_key,
        title = stringResource(id = R.string.screen_recovery_key_confirm_title),
        subTitle = stringResource(id = R.string.screen_recovery_key_confirm_description),
    )
}

@Composable
private fun BottomMenu(
    state: SecureBackupEnterRecoveryKeyState,
    onSubmit: () -> Unit,
) {
    ButtonColumnMolecule(
        modifier = Modifier.padding(bottom = 20.dp)
    ) {
        Button(
            text = stringResource(id = CommonStrings.action_confirm),
            enabled = state.isSubmitEnabled,
            showProgress = state.submitAction.isLoading(),
            modifier = Modifier.fillMaxWidth(),
            onClick = onSubmit
        )
    }
}

@Composable
private fun Content(
    state: SecureBackupEnterRecoveryKeyState,
    onChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    RecoveryKeyView(
        modifier = Modifier.padding(top = 52.dp),
        state = state.recoveryKeyViewState,
        onClick = null,
        onChange = onChange,
        onSubmit = onSubmit,
    )
}

@PreviewsDayNight
@Composable
internal fun SecureBackupEnterRecoveryKeyViewPreview(
    @PreviewParameter(SecureBackupEnterRecoveryKeyStateProvider::class) state: SecureBackupEnterRecoveryKeyState
) = ElementPreview {
    SecureBackupEnterRecoveryKeyView(
        state = state,
        onDone = {},
        onBackClicked = {},
    )
}
