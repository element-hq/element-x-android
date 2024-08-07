/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.securebackup.impl.reset.password

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
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
fun ResetKeyPasswordView(
    state: ResetKeyPasswordState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val passwordState = textFieldState(stateValue = "")
    FlowStepPage(
        modifier = modifier,
        iconStyle = BigIcon.Style.Default(CompoundIcons.LockSolid()),
        title = stringResource(CommonStrings.screen_reset_encryption_password_title),
        subTitle = stringResource(CommonStrings.screen_reset_encryption_password_subtitle),
        onBackClick = onBack,
        content = { Content(textFieldState = passwordState) },
        buttons = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(CommonStrings.action_reset_identity),
                onClick = { state.eventSink(ResetKeyPasswordEvent.Reset(passwordState.value)) },
                destructive = true,
            )
        }
    )

    if (state.resetAction.isLoading() || state.resetAction.isSuccess()) {
        ProgressDialog()
    } else if (state.resetAction.isFailure()) {
        ErrorDialog(
            content = stringResource(CommonStrings.error_unknown),
            onDismiss = { state.eventSink(ResetKeyPasswordEvent.DismissError) }
        )
    }
}

@Composable
private fun Content(textFieldState: MutableState<String>) {
    var showPassword by remember { mutableStateOf(false) }
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .onTabOrEnterKeyFocusNext(LocalFocusManager.current),
        value = textFieldState.value,
        onValueChange = { text -> textFieldState.value = text },
        label = { Text(stringResource(CommonStrings.common_password)) },
        placeholder = { Text(stringResource(CommonStrings.screen_reset_encryption_password_placeholder)) },
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
        }
    )
}

@PreviewsDayNight
@Composable
internal fun ResetKeyPasswordViewPreview() {
    ElementPreview {
        ResetKeyPasswordView(
            state = ResetKeyPasswordState(
                resetAction = AsyncAction.Uninitialized,
                eventSink = {}
            ),
            onBack = {}
        )
    }
}
