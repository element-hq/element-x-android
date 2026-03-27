/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.qrcode

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.core.extensions.orEmpty
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.modifiers.clearFocusOnTap
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.qrcode.QrCodeImage
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrCodeInviteView(
    state: QrCodeInviteState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    BackHandler(
        enabled = true,
        onBack
    )
    Scaffold(
        modifier = modifier.clearFocusOnTap(focusManager),
        topBar = {
            TopAppBar(
                titleStr = stringResource(R.string.screen_qrcode_title),
                navigationIcon = { BackButton(onBack) }
            )
        },

        bottomBar = {
            BottomAppBar(
                containerColor = Color.Transparent
            ) {
                TextButton(
                    onClick = { state.eventSink(QrCodeInviteEvents.ScanQrCode) },
                    text = stringResource(R.string.screen_qrcode_scan_qr_code),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.loginChangeServer)
                )
            }
        }

    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Avatar(
                avatarData = AvatarData(id = state.userId.value, name = state.displayName, size = AvatarSize.UserQrCode),
                avatarType = AvatarType.User,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = state.displayName ?: state.userId.value,
                style = ElementTheme.typography.fontHeadingMdRegular,
                textAlign = TextAlign.Center
            )
            if (state.displayName != null) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = state.userId.value,
                    style = ElementTheme.typography.fontBodyMdRegular,
                    textAlign = TextAlign.Center
                )
            }

            if (state.loading) {
                Box(
                    modifier = Modifier.size(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.qrCodeContent != null) {
                QrCodeImage(
                    data = state.qrCodeContent,
                    modifier = Modifier.size(220.dp)
                )
            } else {
                Box(
                    modifier = Modifier.size(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(R.string.screen_qr_code_failure_descrption))
                }
            }

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.screen_qr_code_description),
                textAlign = TextAlign.Center
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun QrCodeInviteViewPreview(
    @PreviewParameter(QrCodeInviteStateProvider ::class) state: QrCodeInviteState
) = ElementPreview {
    QrCodeInviteView(
        state = state,
        onBack = {}
    )
}
