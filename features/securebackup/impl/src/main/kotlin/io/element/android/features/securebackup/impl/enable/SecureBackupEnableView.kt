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
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.composeutils.annotations.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.CommonDrawables

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecureBackupEnableView(
    state: SecureBackupEnableState,
    onDone: () -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(state.enableAction) {
        if (state.enableAction is Async.Success) {
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
    )
    if (state.enableAction is Async.Failure) {
        ErrorDialog(
            content = state.enableAction.error.let { it.message ?: it.toString() },
            onDismiss = { state.eventSink.invoke(SecureBackupEnableEvents.DismissDialog) },
        )
    }
}

@Composable
private fun HeaderContent(
    modifier: Modifier = Modifier,
) {
    IconTitleSubtitleMolecule(
        modifier = modifier.padding(top = 0.dp),
        iconResourceId = CommonDrawables.ic_key,
        title = stringResource(id = R.string.screen_chat_backup_key_backup_action_enable),
        subTitle = null,
    )
}

@Composable
private fun BottomMenu(
    state: SecureBackupEnableState,
) {
    ButtonColumnMolecule(
        modifier = Modifier.padding(bottom = 20.dp)
    ) {
        Button(
            text = stringResource(id = R.string.screen_chat_backup_key_backup_action_enable),
            showProgress = state.enableAction.isLoading(),
            modifier = Modifier.fillMaxWidth(),
            onClick = { state.eventSink.invoke(SecureBackupEnableEvents.EnableBackup) }
        )
    }
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
