/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.linknewdevice.impl.screens.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.linknewdevice.impl.R
import io.element.android.libraries.designsystem.atomic.organisms.NumberedListOrganism
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.LocalBuildMeta
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.annotatedTextWithBold
import io.element.android.libraries.permissions.api.PermissionsView
import kotlinx.collections.immutable.persistentListOf

/**
 * Desktop notice screen:
 * https://www.figma.com/design/pDlJZGBsri47FNTXMnEdXB/Compound-Android-Templates?node-id=2027-23618
 */
@Composable
fun DesktopNoticeView(
    state: DesktopNoticeState,
    onBackClick: () -> Unit,
    onReadyToScanClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestOnReadyToScanClick by rememberUpdatedState(onReadyToScanClick)
    LaunchedEffect(state.canContinue) {
        if (state.canContinue) {
            latestOnReadyToScanClick()
        }
    }

    val appName = LocalBuildMeta.current.applicationName
    FlowStepPage(
        onBackClick = onBackClick,
        title = stringResource(R.string.screen_link_new_device_desktop_title, appName),
        iconStyle = BigIcon.Style.Default(CompoundIcons.Computer()),
        modifier = modifier,
        isScrollable = true,
        buttons = {
            Button(
                text = stringResource(R.string.screen_link_new_device_desktop_submit),
                onClick = { state.eventSink(DesktopNoticeEvent.Continue) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    ) {
        Column(
            Modifier.fillMaxWidth(),
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            NumberedListOrganism(
                items = persistentListOf(
                    AnnotatedString(stringResource(R.string.screen_link_new_device_desktop_step1, appName)),
                    annotatedTextWithBold(
                        text = stringResource(
                            id = R.string.screen_link_new_device_mobile_step2,
                            stringResource(R.string.screen_link_new_device_mobile_step2_action),
                        ),
                        boldText = stringResource(R.string.screen_link_new_device_mobile_step2_action)
                    ),
                    AnnotatedString(stringResource(R.string.screen_link_new_device_desktop_step3)),
                )
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(ElementTheme.colors.bgCriticalSubtle)
                    .padding(16.dp)
                    .fillMaxWidth(),
                text = stringResource(R.string.screen_link_new_device_desktop_warning),
                style = ElementTheme.typography.fontBodyMdMedium,
                color = ElementTheme.colors.textCriticalPrimary,
            )
        }
    }

    PermissionsView(
        title = stringResource(R.string.screen_qr_code_login_no_camera_permission_state_title),
        content = stringResource(R.string.screen_qr_code_login_no_camera_permission_state_description, appName),
        icon = { Icon(imageVector = CompoundIcons.TakePhotoSolid(), contentDescription = null) },
        state = state.cameraPermissionState,
    )
}

@PreviewsDayNight
@Composable
internal fun DesktopNoticeViewPreview(
    @PreviewParameter(DesktopNoticeStatePreviewParam::class) state: DesktopNoticeState,
) = ElementPreview {
    DesktopNoticeView(
        state = state,
        onBackClick = { },
        onReadyToScanClick = { },
    )
}
