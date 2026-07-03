/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.linknewdevice.impl.screens.root

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.linknewdevice.impl.R
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.atomic.atoms.LoadingButtonAtom
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * Device selection screen:
 * https://www.figma.com/design/pDlJZGBsri47FNTXMnEdXB/Compound-Android-Templates?node-id=2027-23616
 * Not supported screen:
 * https://www.figma.com/design/pDlJZGBsri47FNTXMnEdXB/Compound-Android-Templates?node-id=2186-70004
 */
@Composable
fun LinkNewDeviceRootView(
    state: LinkNewDeviceRootState,
    onBackClick: () -> Unit,
    onUnlockDevice: (type: LinkDeviceType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val (title, subtitle, iconStyle) = if (state.isSupported.dataOrNull() == false) {
        Triple(
            stringResource(R.string.screen_link_new_device_error_not_supported_title),
            stringResource(R.string.screen_link_new_device_error_not_supported_subtitle),
            BigIcon.Style.AlertSolid
        )
    } else {
        Triple(
            stringResource(R.string.screen_link_new_device_root_title),
            null,
            BigIcon.Style.Default(CompoundIcons.Devices())
        )
    }

    FlowStepPage(
        onBackClick = onBackClick,
        title = title,
        subTitle = subtitle,
        iconStyle = iconStyle,
        buttons = {
            when (state.isSupported) {
                is AsyncData.Uninitialized,
                is AsyncData.Loading -> {
                    LoadingButtonAtom()
                }
                is AsyncData.Failure -> {
                    Text(
                        text = stringResource(id = CommonStrings.error_unknown),
                        color = ElementTheme.colors.textCriticalPrimary,
                        style = ElementTheme.typography.fontBodyMdRegular,
                        textAlign = TextAlign.Center,
                    )
                    Button(
                        onClick = onBackClick,
                        text = stringResource(CommonStrings.action_dismiss),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                is AsyncData.Success -> {
                    if (state.isSupported.data) {
                        val canClick = state.qrCodeData is AsyncData.Uninitialized
                        val isLoading = state.qrCodeData is AsyncData.Loading || state.qrCodeData is AsyncData.Success
                        Button(
                            onClick = {
                                if (canClick) {
                                    onUnlockDevice(LinkDeviceType.Mobile)
                                }
                            },
                            text = stringResource(
                                id = if (isLoading) {
                                    R.string.screen_link_new_device_root_loading_qr_code
                                } else {
                                    R.string.screen_link_new_device_root_mobile_device
                                }
                            ),
                            showProgress = isLoading,
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = IconSource.Vector(CompoundIcons.Mobile()),
                        )
                        Button(
                            onClick = {
                                if (canClick) {
                                    onUnlockDevice(LinkDeviceType.Desktop)
                                }
                            },
                            text = stringResource(id = R.string.screen_link_new_device_root_desktop_computer),
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = IconSource.Vector(CompoundIcons.Computer()),
                        )
                    } else {
                        Button(
                            onClick = onBackClick,
                            text = stringResource(CommonStrings.action_dismiss),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        },
        modifier = modifier,
    )

    val failure = state.qrCodeData.errorOrNull()
    if (failure != null) {
        ErrorDialog(
            content = failure.message ?: stringResource(CommonStrings.error_unknown),
            onSubmit = { state.eventSink(LinkNewDeviceRootEvent.CloseDialog) },
        )
    }
}

@PreviewsDayNight
@Composable
internal fun LinkNewDeviceRootViewPreview(
    @PreviewParameter(LinkNewDeviceRootStateProvider::class) state: LinkNewDeviceRootState
) = ElementPreview {
    LinkNewDeviceRootView(
        state = state,
        onBackClick = { },
        onUnlockDevice = { },
    )
}
