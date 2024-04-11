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

package io.element.android.features.login.impl.screens.qrcode.connectionnotsecure

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.login.impl.R
import io.element.android.libraries.designsystem.atomic.organisms.NumberedListOrganism
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Text
import kotlinx.collections.immutable.persistentListOf

@Composable
fun QrCodeConnectionNotSecureView(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
) {
    BackHandler {
        onRetry()
    }
    FlowStepPage(
        modifier = modifier,
        iconStyle = BigIcon.Style.AlertSolid,
        title = stringResource(R.string.screen_qr_code_login_connection_note_secure_state_title),
        subTitle = stringResource(R.string.screen_qr_code_login_connection_note_secure_state_description),
        content = { Content() },
        buttons = { Buttons(onRetry) }
    )
}

@Composable
private fun Content() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.screen_qr_code_login_connection_note_secure_state_list_header),
            style = ElementTheme.typography.fontBodyLgMedium,
            textAlign = TextAlign.Center,
        )
        NumberedListOrganism(
            items = persistentListOf(
                AnnotatedString(stringResource(R.string.screen_qr_code_login_connection_note_secure_state_list_item_1)),
                AnnotatedString(stringResource(R.string.screen_qr_code_login_connection_note_secure_state_list_item_2)),
                AnnotatedString(stringResource(R.string.screen_qr_code_login_connection_note_secure_state_list_item_3)),
            )
        )
    }
}

@Composable
private fun Buttons(onRetry: () -> Unit) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.screen_qr_code_login_start_over_button),
        onClick = onRetry
    )
}

@PreviewsDayNight
@Composable
internal fun QrCodeConnectionNotSecureViewPreview() {
    ElementPreview {
        QrCodeConnectionNotSecureView(
            onRetry = {}
        )
    }
}
