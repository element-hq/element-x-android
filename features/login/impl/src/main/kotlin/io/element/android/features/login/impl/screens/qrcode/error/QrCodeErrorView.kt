/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.qrcode.error

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.login.impl.R
import io.element.android.features.login.impl.qrcode.QrCodeErrorScreenType
import io.element.android.libraries.designsystem.atomic.organisms.NumberedListOrganism
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.persistentListOf

@Composable
fun QrCodeErrorView(
    errorScreenType: QrCodeErrorScreenType,
    appName: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onRetry)
    FlowStepPage(
        modifier = modifier,
        iconStyle = BigIcon.Style.AlertSolid,
        title = titleText(errorScreenType, appName),
        subTitle = subtitleText(errorScreenType, appName),
        content = { Content(errorScreenType) },
        buttons = { Buttons(onRetry) },
    )
}

@Composable
private fun titleText(errorScreenType: QrCodeErrorScreenType, appName: String) = when (errorScreenType) {
    QrCodeErrorScreenType.Cancelled -> stringResource(R.string.screen_qr_code_login_error_cancelled_title)
    QrCodeErrorScreenType.Declined -> stringResource(R.string.screen_qr_code_login_error_declined_title)
    QrCodeErrorScreenType.Expired -> stringResource(R.string.screen_qr_code_login_error_expired_title)
    QrCodeErrorScreenType.ProtocolNotSupported -> stringResource(R.string.screen_qr_code_login_error_linking_not_suported_title)
    QrCodeErrorScreenType.InsecureChannelDetected -> stringResource(id = R.string.screen_qr_code_login_connection_note_secure_state_title)
    QrCodeErrorScreenType.SlidingSyncNotAvailable -> stringResource(id = R.string.screen_qr_code_login_error_sliding_sync_not_supported_title, appName)
    is QrCodeErrorScreenType.UnknownError -> stringResource(CommonStrings.common_something_went_wrong)
}

@Composable
private fun subtitleText(errorScreenType: QrCodeErrorScreenType, appName: String) = when (errorScreenType) {
    QrCodeErrorScreenType.Cancelled -> stringResource(R.string.screen_qr_code_login_error_cancelled_subtitle)
    QrCodeErrorScreenType.Declined -> stringResource(R.string.screen_qr_code_login_error_declined_subtitle)
    QrCodeErrorScreenType.Expired -> stringResource(R.string.screen_qr_code_login_error_expired_subtitle)
    QrCodeErrorScreenType.ProtocolNotSupported -> stringResource(R.string.screen_qr_code_login_error_linking_not_suported_subtitle, appName)
    QrCodeErrorScreenType.InsecureChannelDetected -> stringResource(id = R.string.screen_qr_code_login_connection_note_secure_state_description)
    QrCodeErrorScreenType.SlidingSyncNotAvailable -> stringResource(id = R.string.screen_qr_code_login_error_sliding_sync_not_supported_subtitle, appName)
    is QrCodeErrorScreenType.UnknownError -> stringResource(R.string.screen_qr_code_login_unknown_error_description)
}

@Composable
private fun ColumnScope.InsecureChannelDetectedError() {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        text = stringResource(R.string.screen_qr_code_login_connection_note_secure_state_list_header),
        style = ElementTheme.typography.fontBodyLgMedium,
        textAlign = TextAlign.Center,
    )
    NumberedListOrganism(
        modifier = Modifier.fillMaxSize(),
        items = persistentListOf(
            AnnotatedString(stringResource(R.string.screen_qr_code_login_connection_note_secure_state_list_item_1)),
            AnnotatedString(stringResource(R.string.screen_qr_code_login_connection_note_secure_state_list_item_2)),
            AnnotatedString(stringResource(R.string.screen_qr_code_login_connection_note_secure_state_list_item_3)),
        )
    )
}

@Composable
private fun Content(errorScreenType: QrCodeErrorScreenType) {
    when (errorScreenType) {
        QrCodeErrorScreenType.InsecureChannelDetected -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                InsecureChannelDetectedError()
            }
        }
        else -> Unit
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
internal fun QrCodeErrorViewPreview(@PreviewParameter(QrCodeErrorScreenTypeProvider::class) errorScreenType: QrCodeErrorScreenType) {
    ElementPreview {
        QrCodeErrorView(
            errorScreenType = errorScreenType,
            appName = "Element X",
            onRetry = {}
        )
    }
}

class QrCodeErrorScreenTypeProvider : PreviewParameterProvider<QrCodeErrorScreenType> {
    override val values: Sequence<QrCodeErrorScreenType> = sequenceOf(
        QrCodeErrorScreenType.Cancelled,
        QrCodeErrorScreenType.Declined,
        QrCodeErrorScreenType.Expired,
        QrCodeErrorScreenType.ProtocolNotSupported,
        QrCodeErrorScreenType.InsecureChannelDetected,
        QrCodeErrorScreenType.SlidingSyncNotAvailable,
        QrCodeErrorScreenType.UnknownError
    )
}
