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

import androidx.activity.compose.BackHandler
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
import io.element.android.features.login.impl.R
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
    step: QrCodeConfirmationStep,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler {
        onCancel()
    }
    val icon = when (step) {
        is QrCodeConfirmationStep.DisplayCheckCode -> CompoundIcons.Computer()
        is QrCodeConfirmationStep.DisplayVerificationCode -> CompoundIcons.LockSolid()
    }
    val title = when (step) {
        is QrCodeConfirmationStep.DisplayCheckCode -> stringResource(R.string.screen_qr_code_login_device_code_title)
        is QrCodeConfirmationStep.DisplayVerificationCode -> stringResource(R.string.screen_qr_code_login_verify_code_title)
    }
    val subtitle = when (step) {
        is QrCodeConfirmationStep.DisplayCheckCode -> stringResource(R.string.screen_qr_code_login_device_code_subtitle)
        is QrCodeConfirmationStep.DisplayVerificationCode -> stringResource(R.string.screen_qr_code_login_verify_code_subtitle)
    }
    FlowStepPage(
        modifier = modifier,
        iconStyle = BigIcon.Style.Default(icon),
        title = title,
        subTitle = subtitle,
        content = { Content(step = step) },
        buttons = { Buttons(onCancel = onCancel) }
    )
}

@Composable
private fun Content(step: QrCodeConfirmationStep) {
    Column(
        modifier = Modifier.padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (step) {
            is QrCodeConfirmationStep.DisplayCheckCode -> {
                Digits(code = step.code)
                Spacer(modifier = Modifier.height(32.dp))
                WaitingForOtherDevice()
            }
            is QrCodeConfirmationStep.DisplayVerificationCode -> {
                Digits(code = step.code)
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
        Text(
            text = stringResource(R.string.screen_qr_code_login_verify_code_loading),
            style = ElementTheme.typography.fontBodySmRegular,
            color = ElementTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Buttons(
    onCancel: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(CommonStrings.action_cancel),
            onClick = onCancel
        )
    }
}

@PreviewsDayNight
@Composable
internal fun QrCodeConfirmationViewPreview(@PreviewParameter(QrCodeConfirmationStepPreviewProvider::class) step: QrCodeConfirmationStep) {
    ElementPreview {
        QrCodeConfirmationView(
            step = step,
            onCancel = {},
        )
    }
}
