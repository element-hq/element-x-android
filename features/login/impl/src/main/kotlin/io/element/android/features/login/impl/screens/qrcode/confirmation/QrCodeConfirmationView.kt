/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.login.impl.screens.qrcode.confirmation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun QrCodeConfirmationView(
    state: QrCodeConfirmationState,
    modifier: Modifier = Modifier,
) {
    val icon = when (state.step) {
        is QrCodeConfirmationStep.DisplayCheckCode -> CompoundIcons.Computer()
        is QrCodeConfirmationStep.DisplayVerificationCode -> CompoundIcons.LockSolid()
    }
    // TODO: localazy
    val title = when (state.step) {
        is QrCodeConfirmationStep.DisplayCheckCode -> "Enter the number below on your device"
        is QrCodeConfirmationStep.DisplayVerificationCode -> "Your verification code"
    }
    // TODO: localazy
    val subtitle = when (state.step) {
        is QrCodeConfirmationStep.DisplayCheckCode -> "You’ll be asked to enter the two digits shown below."
        is QrCodeConfirmationStep.DisplayVerificationCode -> "Your account provider may ask for the following code to verify the sign in."
    }
    FlowStepPage(
        modifier = modifier,
        iconStyle = BigIcon.Style.Default(icon),
        title = title,
        subTitle = subtitle,
        content = { Content(state = state) },
        buttons = { Buttons(state = state) }
    )
}

@Composable
private fun Content(state: QrCodeConfirmationState) {
    Column(
        modifier = Modifier.padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (state.step) {
            is QrCodeConfirmationStep.DisplayCheckCode -> {
                Digits(code = state.step.code)
                Spacer(modifier = Modifier.height(32.dp))
                WaitingForOtherDevice()
            }
            is QrCodeConfirmationStep.DisplayVerificationCode -> {
                Digits(code = state.step.code)
                Spacer(modifier = Modifier.height(32.dp))
                WaitingForOtherDevice()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Digits(code: String) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        code.forEach {
            Text(
                modifier = Modifier
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ElementTheme.colors.bgActionSecondaryPressed)
                    .padding(horizontal = 16.dp, vertical = 17.dp),
                text = it.toString()
            )
        }
    }
}

@Composable
private fun WaitingForOtherDevice() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(20.dp)
                .padding(2.dp),
            strokeWidth = 2.dp,
        )
        // TODO: localazy
        Text(
            text = "Waiting for your other device",
            style = ElementTheme.typography.fontBodySmRegular,
            color = ElementTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Buttons(state: QrCodeConfirmationState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(CommonStrings.action_cancel), onClick = { /*TODO*/ }
        )
    }
}

@PreviewsDayNight
@Composable
internal fun QrCodeConfirmationViewPreview(@PreviewParameter(QrConfirmationCodeStatePreviewProvider::class) state: QrCodeConfirmationState) {
    ElementPreview {
        QrCodeConfirmationView(state = state)
    }
}
