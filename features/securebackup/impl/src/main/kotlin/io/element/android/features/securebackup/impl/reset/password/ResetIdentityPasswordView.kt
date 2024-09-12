/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.reset.password

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.securebackup.impl.R
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.form.textFieldState
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.OutlinedTextField
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.onTabOrEnterKeyFocusNext
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun ResetIdentityPasswordView(
    state: ResetIdentityPasswordState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val passwordState = textFieldState(stateValue = "")
    FlowStepPage(
        modifier = modifier,
        iconStyle = BigIcon.Style.Default(CompoundIcons.LockSolid()),
        title = stringResource(R.string.screen_reset_encryption_password_title),
        subTitle = stringResource(R.string.screen_reset_encryption_password_subtitle),
        onBackClick = onBack,
        content = {
            Content(
                text = passwordState.value,
                onTextChange = { newText ->
                    if (state.resetAction.isFailure()) {
                        state.eventSink(ResetIdentityPasswordEvent.DismissError)
                    }
                    passwordState.value = newText
                },
                hasError = state.resetAction.isFailure(),
            )
        },
        buttons = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(CommonStrings.action_reset_identity),
                onClick = { state.eventSink(ResetIdentityPasswordEvent.Reset(passwordState.value)) },
                destructive = true,
                enabled = passwordState.value.isNotEmpty(),
            )
        }
    )

    // On success we need to wait until the screen is automatically dismissed, so we keep the progress dialog
    if (state.resetAction.isLoading() || state.resetAction.isSuccess()) {
        ProgressDialog()
    }
}

@Composable
private fun Content(text: String, onTextChange: (String) -> Unit, hasError: Boolean) {
    var showPassword by remember { mutableStateOf(false) }
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .onTabOrEnterKeyFocusNext(LocalFocusManager.current),
        value = text,
        onValueChange = onTextChange,
        label = { Text(stringResource(CommonStrings.common_password)) },
        placeholder = { Text(stringResource(R.string.screen_reset_encryption_password_placeholder)) },
        singleLine = true,
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val image =
                if (showPassword) CompoundIcons.VisibilityOn() else CompoundIcons.VisibilityOff()
            val description =
                if (showPassword) stringResource(CommonStrings.a11y_hide_password) else stringResource(CommonStrings.a11y_show_password)

            IconButton(onClick = { showPassword = !showPassword }) {
                Icon(imageVector = image, description)
            }
        },
        isError = hasError,
        supportingText = if (hasError) {
            { Text(stringResource(R.string.screen_reset_encryption_password_error)) }
        } else {
            null
        }
    )
}

@PreviewsDayNight
@Composable
internal fun ResetIdentityPasswordViewPreview(@PreviewParameter(ResetIdentityPasswordStateProvider::class) state: ResetIdentityPasswordState) {
    ElementPreview {
        ResetIdentityPasswordView(
            state = state,
            onBack = {}
        )
    }
}
