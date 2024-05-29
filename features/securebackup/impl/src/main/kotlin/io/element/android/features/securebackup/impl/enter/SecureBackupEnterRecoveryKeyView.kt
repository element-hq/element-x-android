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

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.securebackup.impl.R
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyView
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun SecureBackupEnterRecoveryKeyView(
    state: SecureBackupEnterRecoveryKeyState,
    onSuccess: () -> Unit,
    onBackClick: () -> Unit,
    onCreateNewRecoveryKey: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AsyncActionView(
        async = state.submitAction,
        onSuccess = { onSuccess() },
        progressDialog = { },
        errorTitle = { stringResource(id = R.string.screen_recovery_key_confirm_error_title) },
        errorMessage = { stringResource(id = R.string.screen_recovery_key_confirm_error_content) },
        onErrorDismiss = { state.eventSink(SecureBackupEnterRecoveryKeyEvents.ClearDialog) },
    )

    FlowStepPage(
        modifier = modifier,
        onBackClick = onBackClick,
        iconVector = CompoundIcons.KeySolid(),
        title = stringResource(id = R.string.screen_recovery_key_confirm_title),
        subTitle = stringResource(id = R.string.screen_recovery_key_confirm_description),
        content = { Content(state = state) },
        buttons = { Buttons(state = state, onCreateRecoveryKey = onCreateNewRecoveryKey) }
    )
}

@Composable
private fun Content(
    state: SecureBackupEnterRecoveryKeyState,
) {
    RecoveryKeyView(
        modifier = Modifier.padding(top = 52.dp),
        state = state.recoveryKeyViewState,
        onClick = null,
        onChange = {
            state.eventSink.invoke(SecureBackupEnterRecoveryKeyEvents.OnRecoveryKeyChange(it))
        },
        onSubmit = {
            state.eventSink.invoke(SecureBackupEnterRecoveryKeyEvents.Submit)
        },
    )
}

@Composable
private fun ColumnScope.Buttons(
    state: SecureBackupEnterRecoveryKeyState,
    onCreateRecoveryKey: () -> Unit,
) {
    Button(
        text = stringResource(id = CommonStrings.action_continue),
        enabled = state.isSubmitEnabled,
        showProgress = state.submitAction.isLoading(),
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            state.eventSink.invoke(SecureBackupEnterRecoveryKeyEvents.Submit)
        }
    )
    TextButton(
        text = stringResource(id = R.string.screen_recovery_key_confirm_lost_recovery_key),
        enabled = !state.submitAction.isLoading(),
        modifier = Modifier.fillMaxWidth(),
        onClick = onCreateRecoveryKey,
    )
}

@PreviewsDayNight
@Composable
internal fun SecureBackupEnterRecoveryKeyViewPreview(
    @PreviewParameter(SecureBackupEnterRecoveryKeyStateProvider::class) state: SecureBackupEnterRecoveryKeyState
) = ElementPreview {
    SecureBackupEnterRecoveryKeyView(
        state = state,
        onSuccess = {},
        onBackClick = {},
        onCreateNewRecoveryKey = {},
    )
}
