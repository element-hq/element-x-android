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

package io.element.android.features.securebackup.impl.enable

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.securebackup.impl.R
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.utils.CommonDrawables

@Composable
fun SecureBackupEnableView(
    state: SecureBackupEnableState,
    onDone: () -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(state.enableAction) {
        if (state.enableAction is AsyncData.Success) {
            onDone()
        }
    }
    FlowStepPage(
        modifier = modifier,
        onBackClicked = onBackClicked,
        title = stringResource(id = R.string.screen_chat_backup_key_backup_action_enable),
        iconVector = ImageVector.vectorResource(CommonDrawables.ic_key),
        buttons = { Buttons(state = state) }
    )
    if (state.enableAction is AsyncData.Failure) {
        ErrorDialog(
            content = state.enableAction.error.let { it.message ?: it.toString() },
            onDismiss = { state.eventSink.invoke(SecureBackupEnableEvents.DismissDialog) },
        )
    }
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
        onDone = {},
        onBackClicked = {},
    )
}
