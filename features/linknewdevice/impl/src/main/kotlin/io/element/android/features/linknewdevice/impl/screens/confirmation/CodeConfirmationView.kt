/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.linknewdevice.impl.screens.confirmation

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
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.linknewdevice.impl.R
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun CodeConfirmationView(
    code: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onCancel)
    FlowStepPage(
        modifier = modifier,
        iconStyle = BigIcon.Style.Default(CompoundIcons.Computer()),
        title = stringResource(R.string.screen_qr_code_login_device_code_title),
        subTitle = stringResource(R.string.screen_qr_code_login_device_code_subtitle),
        content = { Content(code = code) },
        buttons = { Buttons(onCancel = onCancel) }
    )
}

@Composable
private fun Content(code: String) {
    Column(
        modifier = Modifier.padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Digits(code = code)
        Spacer(modifier = Modifier.height(32.dp))
        WaitingForOtherDevice()
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
            onClick = onCancel,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun CodeConfirmationViewPreview() {
    ElementPreview {
        CodeConfirmationView(
            code = "67",
            onCancel = {},
        )
    }
}
