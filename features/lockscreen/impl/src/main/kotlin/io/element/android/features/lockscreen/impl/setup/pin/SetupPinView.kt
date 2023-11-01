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

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.lockscreen.impl.setup.pin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.lockscreen.impl.R
import io.element.android.features.lockscreen.impl.components.PinEntryTextField
import io.element.android.features.lockscreen.impl.setup.pin.validation.SetupPinFailure
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.TopAppBar

@Composable
fun SetupPinView(
    state: SetupPinState,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton(onClick = onBackClicked)
                },
                title = {}
            )
        },
        content = { padding ->
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .imePadding()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .verticalScroll(state = scrollState)
                    .padding(vertical = 16.dp, horizontal = 20.dp),
            ) {
                SetupPinHeader(state.isConfirmationStep, state.appName)
                SetupPinContent(state)
            }
        }
    )
}

@Composable
private fun SetupPinHeader(
    isValidationStep: Boolean,
    appName: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconTitleSubtitleMolecule(
            title = if (isValidationStep) {
                stringResource(id = R.string.screen_app_lock_setup_confirm_pin)
            } else {
                stringResource(id = R.string.screen_app_lock_setup_choose_pin)
            },
            subTitle = stringResource(id = R.string.screen_app_lock_setup_pin_context, appName),
            iconImageVector = Icons.Filled.Lock,
        )
    }
}

@Composable
private fun SetupPinContent(
    state: SetupPinState,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    PinEntryTextField(
        pinEntry = state.activePinEntry,
        isSecured = true,
        onValueChange = {
            state.eventSink(SetupPinEvents.OnPinEntryChanged(it))
        },
        modifier = modifier
            .focusRequester(focusRequester)
            .padding(top = 36.dp)
            .fillMaxWidth()
    )
    if (state.setupPinFailure != null) {
        ErrorDialog(
            modifier = modifier,
            title = state.setupPinFailure.title(),
            content = state.setupPinFailure.content(),
            onDismiss = {
                state.eventSink(SetupPinEvents.ClearFailure)
            }
        )
    }
}

@Composable
private fun SetupPinFailure.content(): String {
    return when (this) {
        SetupPinFailure.PinBlacklisted -> stringResource(id = R.string.screen_app_lock_setup_pin_blacklisted_dialog_content)
        SetupPinFailure.PinsDontMatch -> stringResource(id = R.string.screen_app_lock_setup_pin_mismatch_dialog_content)
    }
}

@Composable
private fun SetupPinFailure.title(): String {
    return when (this) {
        SetupPinFailure.PinBlacklisted -> stringResource(id = R.string.screen_app_lock_setup_pin_blacklisted_dialog_title)
        SetupPinFailure.PinsDontMatch -> stringResource(id = R.string.screen_app_lock_setup_pin_mismatch_dialog_title)
    }
}

@Composable
@PreviewsDayNight
internal fun SetupPinViewPreview(@PreviewParameter(SetupPinStateProvider::class) state: SetupPinState) {
    ElementPreview {
        SetupPinView(
            state = state,
            onBackClicked = {},
        )
    }
}
