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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.login.impl.R
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.modifiers.cornerBorder
import io.element.android.libraries.designsystem.modifiers.squareSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import io.element.android.libraries.matrix.api.auth.qrlogin.QrLoginException
import io.element.android.libraries.qrcode.QrCodeCameraView

@Composable
fun QrCodeScanView(
    state: QrCodeScanState,
    onBackClicked: () -> Unit,
    onQrCodeDataReady: (MatrixQrCodeLoginData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val updatedOnQrCodeDataReady by rememberUpdatedState(onQrCodeDataReady)
    // QR code data parsed successfully, notify the parent node
    if (state.authenticationAction is AsyncAction.Success) {
        LaunchedEffect(state.authenticationAction, updatedOnQrCodeDataReady) {
            updatedOnQrCodeDataReady(state.authenticationAction.data)
        }
    }

    FlowStepPage(
        modifier = modifier,
        onBackClicked = onBackClicked,
        iconStyle = BigIcon.Style.Default(CompoundIcons.Computer()),
        title = stringResource(R.string.screen_qr_code_login_scanning_state_title),
        content = { Content(state = state) },
        buttons = { Buttons(state = state) }
    )
}

@Composable
private fun Content(
    state: QrCodeScanState,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val modifier = if (constraints.maxWidth > constraints.maxHeight) {
            Modifier.fillMaxHeight()
        } else {
            Modifier.fillMaxWidth()
        }.then(
            Modifier
                .padding(start = 20.dp, end = 20.dp, top = 50.dp)
                .squareSize()
                .cornerBorder(
                    strokeWidth = 4.dp,
                    color = ElementTheme.colors.textPrimary,
                    cornerSizeDp = 42.dp,
                )
        )
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            QrCodeCameraView(
                modifier = Modifier.fillMaxSize(),
                onQrCodeScanned = { state.eventSink.invoke(QrCodeScanEvents.QrCodeScanned(it)) },
                renderPreview = state.isScanning,
            )
        }
    }
}

@Composable
private fun ColumnScope.Buttons(
    state: QrCodeScanState,
) {
    Column(Modifier.heightIn(min = 130.dp)) {
        when (state.authenticationAction) {
            is AsyncAction.Failure -> {
                Button(
                    text = stringResource(id = R.string.screen_qr_code_login_invalid_scan_state_retry_button),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    onClick = {
                        state.eventSink.invoke(QrCodeScanEvents.TryAgain)
                    }
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    val error = state.authenticationAction.error
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
                            text = when (error) {
                                is QrLoginException.OtherDeviceNotSignedIn -> {
                                    stringResource(R.string.screen_qr_code_login_device_not_signed_in_scan_state_title)
                                }
                                else -> stringResource(R.string.screen_qr_code_login_invalid_scan_state_subtitle)
                            },
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error,
                            style = ElementTheme.typography.fontBodySmMedium,
                        )
                    }
                    Text(
                        text = when (error) {
                            is QrLoginException.OtherDeviceNotSignedIn -> stringResource(R.string.screen_qr_code_login_device_not_signed_in_scan_state_subtitle)
                            else -> stringResource(R.string.screen_qr_code_login_invalid_scan_state_description)
                        },
                        textAlign = TextAlign.Center,
                        style = ElementTheme.typography.fontBodySmRegular,
                        color = ElementTheme.colors.textSecondary,
                    )
                }
            }
            AsyncAction.Loading, is AsyncAction.Success -> {
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
                        text = stringResource(R.string.screen_qr_code_login_connecting_subtitle),
                        textAlign = TextAlign.Center,
                        style = ElementTheme.typography.fontBodySmRegular,
                        color = ElementTheme.colors.textSecondary,
                    )
                }
            }
            AsyncAction.Uninitialized,
            AsyncAction.Confirming -> Unit
        }
    }
}

@PreviewsDayNight
@Composable
internal fun QrCodeScanViewPreview(@PreviewParameter(QrCodeScanStateProvider::class) state: QrCodeScanState) = ElementPreview {
    QrCodeScanView(
        state = state,
        onQrCodeDataReady = {},
        onBackClicked = {},
    )
}
