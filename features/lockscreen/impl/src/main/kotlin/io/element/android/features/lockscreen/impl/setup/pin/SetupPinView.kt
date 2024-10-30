/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.TopAppBar

@Composable
fun SetupPinView(
    state: SetupPinState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton(onClick = onBackClick)
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
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconTitleSubtitleMolecule(
            title = if (isValidationStep) {
                stringResource(id = R.string.screen_app_lock_setup_confirm_pin)
            } else {
                stringResource(id = R.string.screen_app_lock_setup_choose_pin)
            },
            subTitle = stringResource(id = R.string.screen_app_lock_setup_pin_context, appName),
            iconStyle = BigIcon.Style.Default(Icons.Filled.Lock),
        )
    }
}

@Composable
private fun SetupPinContent(
    state: SetupPinState,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    PinEntryTextField(
        pinEntry = state.activePinEntry,
        isSecured = true,
        onValueChange = { entry ->
            state.eventSink(SetupPinEvents.OnPinEntryChanged(entry, state.isConfirmationStep))
        },
        modifier = Modifier
            .focusRequester(focusRequester)
            .padding(top = 36.dp)
            .fillMaxWidth()
    )
    if (state.setupPinFailure != null) {
        ErrorDialog(
            title = state.setupPinFailure.title(),
            content = state.setupPinFailure.content(),
            onSubmit = {
                state.eventSink(SetupPinEvents.ClearFailure)
            }
        )
    }
}

@Composable
private fun SetupPinFailure.content(): String {
    return when (this) {
        SetupPinFailure.ForbiddenPin -> stringResource(id = R.string.screen_app_lock_setup_pin_forbidden_dialog_content)
        SetupPinFailure.PinsDoNotMatch -> stringResource(id = R.string.screen_app_lock_setup_pin_mismatch_dialog_content)
    }
}

@Composable
private fun SetupPinFailure.title(): String {
    return when (this) {
        SetupPinFailure.ForbiddenPin -> stringResource(id = R.string.screen_app_lock_setup_pin_forbidden_dialog_title)
        SetupPinFailure.PinsDoNotMatch -> stringResource(id = R.string.screen_app_lock_setup_pin_mismatch_dialog_title)
    }
}

@Composable
@PreviewsDayNight
internal fun SetupPinViewPreview(@PreviewParameter(SetupPinStateProvider::class) state: SetupPinState) {
    ElementPreview {
        SetupPinView(
            state = state,
            onBackClick = {},
        )
    }
}
