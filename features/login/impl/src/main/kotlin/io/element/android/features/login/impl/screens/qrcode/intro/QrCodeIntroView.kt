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

package io.element.android.features.login.impl.screens.qrcode.intro

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.atomic.organisms.InfoListItem
import io.element.android.libraries.designsystem.atomic.organisms.InfoListOrganism
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.permissions.api.PermissionsView
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.persistentListOf

@Composable
fun QrCodeIntroView(
    state: QrCodeIntroState,
    onBackClicked: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestOnContinue by rememberUpdatedState(onContinue)
    LaunchedEffect(state.canContinue) {
        if (state.canContinue) {
            latestOnContinue()
        }
    }
    FlowStepPage(
        modifier = modifier,
        onBackClicked = onBackClicked,
        iconVector = CompoundIcons.Computer(),
        title = "Open ${state.appName} to another device to get the QR code", // TODO Localazy
        content = { Content(state = state) },
        buttons = { Buttons(state = state) }
    )

    PermissionsView(
        state = state.cameraPermissionState,
    )
}

@Composable
private fun Content(
    state: QrCodeIntroState,
) {
    // TODO integrate final design
    InfoListOrganism(
        modifier = Modifier.padding(top = 50.dp),
        items = persistentListOf(
            InfoListItem(
                message = "Open ${state.appName} on a desktop device",
                iconComposable = { NumberIcon(1) },
            ),
            InfoListItem(
                message = "Click on your avatar",
                iconComposable = { NumberIcon(2) },
            ),
            InfoListItem(
                message = "Select ”Link new device”",
                iconComposable = { NumberIcon(3) },
            ),
            InfoListItem(
                message = "Select ”Show QR code”",
                iconComposable = { NumberIcon(4) },
            ),
        ),
        textStyle = ElementTheme.typography.fontBodyMdRegular,
        iconTint = ElementTheme.colors.textPrimary,
        backgroundColor = Color.Transparent
    )
}

@Composable
private fun NumberIcon(i: Int) {
    Text(
        text = i.toString(),
    )
}

@Composable
private fun ColumnScope.Buttons(
    state: QrCodeIntroState,
) {
    Button(
        text = stringResource(id = CommonStrings.action_continue),
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            state.eventSink.invoke(QrCodeIntroEvents.Continue)
        }
    )
}

@PreviewsDayNight
@Composable
internal fun QrCodeIntroViewPreview(@PreviewParameter(QrCodeIntroStateProvider::class) state: QrCodeIntroState) = ElementPreview {
    QrCodeIntroView(
        state = state,
        onBackClicked = {},
        onContinue = {},
    )
}
