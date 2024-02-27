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

package io.element.android.features.login.impl.screens.qrcode.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.modifiers.cornerBorder
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.qrcode.QrCodeCameraView
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun QrCodeScanView(
    state: QrCodeScanState,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowStepPage(
        modifier = modifier,
        onBackClicked = onBackClicked,
        iconVector = CompoundIcons.Computer(),
        title = "Scan the QR code", // TODO Localazy
        content = { Content(state = state) },
        buttons = { Buttons(state = state) }
    )
}

@Composable
private fun Content(
    state: QrCodeScanState,
) {
    val modifier = Modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp, top = 50.dp)
        .aspectRatio(1f)
        .cornerBorder(
            strokeWidth = 4.dp,
            color = ElementTheme.colors.textPrimary,
            cornerSizeDp = 42.dp,
        )
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        if (state.isScanning) {
            QrCodeCameraView(
                modifier = Modifier.fillMaxSize(),
                onQrCodeScanned = { state.eventSink.invoke(QrCodeScanEvents.QrCodeScanned(it)) }
            )
        } else {
            Icon(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                imageVector = CompoundIcons.QrCode(),
                contentDescription = null,
                tint = ElementTheme.colors.textPrimary,
            )
        }
    }
}

@Composable
private fun ColumnScope.Buttons(
    state: QrCodeScanState,
) {
    when (state.authenticationAction) {
        is AsyncAction.Failure -> {
            Button(
                text = stringResource(id = CommonStrings.action_try_again),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    state.eventSink.invoke(QrCodeScanEvents.TryAgain)
                }
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = CompoundIcons.Error(),
                        tint = MaterialTheme.colorScheme.error,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Wrong QR code", // TODO Localazy
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error,
                        style = ElementTheme.typography.fontBodySmMedium,
                    )
                }
                Text(
                    text = "Use the QR code shown on the other device", // TODO Localazy
                    textAlign = TextAlign.Center,
                    style = ElementTheme.typography.fontBodySmRegular,
                    color = ElementTheme.colors.textSecondary,
                )
            }
        }
        AsyncAction.Loading -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .progressSemantics()
                        .size(20.dp),
                    strokeWidth = 2.dp
                )
                Text(
                    text = "Establishing a secure connection", // TODO Localazy
                    textAlign = TextAlign.Center,
                )
            }
        }
        AsyncAction.Uninitialized,
        AsyncAction.Confirming,
        is AsyncAction.Success -> Unit
    }
}

@PreviewsDayNight
@Composable
internal fun QrCodeScanViewPreview(@PreviewParameter(QrCodeScanStateProvider::class) state: QrCodeScanState) = ElementPreview {
    QrCodeScanView(
        state = state,
        onBackClicked = {},
    )
}
