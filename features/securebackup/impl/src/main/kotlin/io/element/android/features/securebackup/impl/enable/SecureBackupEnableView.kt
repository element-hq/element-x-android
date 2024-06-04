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
