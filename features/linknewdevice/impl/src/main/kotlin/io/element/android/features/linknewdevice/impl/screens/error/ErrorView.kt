/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.linknewdevice.impl.screens.error

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
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.linknewdevice.impl.R
import io.element.android.libraries.designsystem.atomic.organisms.NumberedListOrganism
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.LocalBuildMeta
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.persistentListOf

@Composable
fun ErrorView(
    errorScreenType: ErrorScreenType,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val appName = LocalBuildMeta.current.applicationName
    BackHandler(onBack = onCancel)
    val iconStyle = when (errorScreenType) {
        ErrorScreenType.OtherDeviceAlreadySignedIn -> BigIcon.Style.SuccessSolid
        else -> BigIcon.Style.AlertSolid
    }
    FlowStepPage(
        modifier = modifier,
        iconStyle = iconStyle,
        title = titleText(errorScreenType, appName),
        subTitle = subtitleText(errorScreenType, appName),
        content = { Content(errorScreenType) },
        buttons = {
            when (errorScreenType) {
                ErrorScreenType.OtherDeviceAlreadySignedIn -> DoneButton(
                    onDone = onCancel,
                )
                else -> Buttons(
                    onRetry = onRetry,
                    onCancel = onCancel,
                )
            }
        },
    )
}

@Composable
private fun titleText(errorScreenType: ErrorScreenType, appName: String) = when (errorScreenType) {
    ErrorScreenType.Cancelled -> stringResource(R.string.screen_qr_code_login_error_cancelled_title)
    ErrorScreenType.Declined -> stringResource(R.string.screen_qr_code_login_error_declined_title)
    ErrorScreenType.Expired -> stringResource(R.string.screen_qr_code_login_error_expired_title)
    ErrorScreenType.ProtocolNotSupported -> stringResource(R.string.screen_qr_code_login_error_linking_not_suported_title)
    ErrorScreenType.InsecureChannelDetected -> stringResource(id = R.string.screen_qr_code_login_connection_note_secure_state_title)
    ErrorScreenType.Mismatch2Digits -> stringResource(id = R.string.screen_link_new_device_wrong_number_title)
    ErrorScreenType.SlidingSyncNotAvailable -> stringResource(id = R.string.screen_qr_code_login_error_sliding_sync_not_supported_title, appName)
    is ErrorScreenType.UnknownError -> stringResource(CommonStrings.common_something_went_wrong)
    ErrorScreenType.OtherDeviceAlreadySignedIn -> stringResource(R.string.screen_qr_code_login_error_device_already_signed_in_title)
}

@Composable
private fun subtitleText(errorScreenType: ErrorScreenType, appName: String) = when (errorScreenType) {
    ErrorScreenType.Cancelled -> stringResource(R.string.screen_qr_code_login_error_cancelled_subtitle)
    ErrorScreenType.Declined -> stringResource(R.string.screen_qr_code_login_error_declined_subtitle)
    ErrorScreenType.Expired -> stringResource(R.string.screen_qr_code_login_error_expired_subtitle)
    ErrorScreenType.ProtocolNotSupported -> stringResource(R.string.screen_qr_code_login_error_linking_not_suported_subtitle, appName)
    ErrorScreenType.Mismatch2Digits -> stringResource(id = R.string.screen_link_new_device_wrong_number_subtitle)
    ErrorScreenType.InsecureChannelDetected -> stringResource(id = R.string.screen_qr_code_login_connection_note_secure_state_description)
    ErrorScreenType.SlidingSyncNotAvailable -> stringResource(id = R.string.screen_qr_code_login_error_sliding_sync_not_supported_subtitle, appName)
    is ErrorScreenType.UnknownError -> stringResource(R.string.screen_qr_code_login_unknown_error_description)
    ErrorScreenType.OtherDeviceAlreadySignedIn -> stringResource(R.string.screen_qr_code_login_error_device_already_signed_in_subtitle)
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
private fun Content(errorScreenType: ErrorScreenType) {
    when (errorScreenType) {
        ErrorScreenType.InsecureChannelDetected -> {
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
private fun DoneButton(
    onDone: () -> Unit,
) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(CommonStrings.action_done),
        onClick = onDone,
    )
}

@Composable
private fun Buttons(
    onRetry: () -> Unit,
    onCancel: () -> Unit,
) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(CommonStrings.action_try_again),
        onClick = onRetry,
    )
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(CommonStrings.action_cancel),
        onClick = onCancel,
    )
}

@PreviewsDayNight
@Composable
internal fun ErrorViewPreview(@PreviewParameter(ErrorScreenTypeProvider::class) errorScreenType: ErrorScreenType) {
    ElementPreview {
        ErrorView(
            errorScreenType = errorScreenType,
            onRetry = {},
            onCancel = {},
        )
    }
}
