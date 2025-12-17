/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.enter

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.securebackup.impl.R
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyView
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SecureBackupEnterRecoveryKeyView(
    state: SecureBackupEnterRecoveryKeyState,
    onSuccess: () -> Unit,
    onBackClick: () -> Unit,
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
        isScrollable = true,
        onBackClick = onBackClick,
        iconStyle = BigIcon.Style.Default(CompoundIcons.KeySolid()),
        title = stringResource(id = R.string.screen_recovery_key_confirm_title),
        subTitle = stringResource(id = R.string.screen_recovery_key_confirm_description),
        buttons = { Buttons(state = state) }
    ) {
        Content(state = state)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Content(
    state: SecureBackupEnterRecoveryKeyState,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var isFocused by remember { mutableStateOf(false) }
    val isImeVisible = WindowInsets.isImeVisible
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(isImeVisible, isFocused) {
        // When the keyboard is shown, we want to scroll the text field into view
        if (isImeVisible && isFocused) {
            coroutineScope.launch {
                // Delay to ensure the keyboard is fully shown
                delay(100.milliseconds)
                bringIntoViewRequester.bringIntoView()
            }
        }
    }
    RecoveryKeyView(
        modifier = Modifier
            .onFocusChanged { isFocused = it.isFocused }
            .bringIntoViewRequester(bringIntoViewRequester)
            .padding(top = 52.dp, bottom = 32.dp),
        state = state.recoveryKeyViewState,
        onClick = null,
        onChange = {
            state.eventSink.invoke(SecureBackupEnterRecoveryKeyEvents.OnRecoveryKeyChange(it))
        },
        onSubmit = {
            state.eventSink.invoke(SecureBackupEnterRecoveryKeyEvents.Submit)
        },
        toggleRecoveryKeyVisibility = {
            state.eventSink(SecureBackupEnterRecoveryKeyEvents.ChangeRecoveryKeyFieldContentsVisibility(it))
        }
    )
}

@Composable
private fun ColumnScope.Buttons(
    state: SecureBackupEnterRecoveryKeyState,
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
    )
}
