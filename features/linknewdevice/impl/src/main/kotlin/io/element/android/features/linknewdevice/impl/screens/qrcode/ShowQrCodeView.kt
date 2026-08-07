/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.linknewdevice.impl.screens.qrcode

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.linknewdevice.impl.R
import io.element.android.libraries.designsystem.atomic.organisms.NumberedListOrganism
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.LocalBuildMeta
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.utils.annotatedTextWithBold
import io.element.android.libraries.qrcode.QrCodeImage
import kotlinx.collections.immutable.persistentListOf

/**
 * QrCode display screen:
 * https://www.figma.com/design/pDlJZGBsri47FNTXMnEdXB/Compound-Android-Templates?node-id=2027-23617
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ShowQrCodeView(
    state: ShowQrCodeState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val appName = LocalBuildMeta.current.applicationName
    FlowStepPage(
        onBackClick = onBackClick,
        title = stringResource(R.string.screen_link_new_device_mobile_title, appName),
        iconStyle = BigIcon.Style.Default(CompoundIcons.TakePhotoSolid()),
        modifier = modifier,
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedContent(
                modifier = Modifier.size(220.dp),
                targetState = state.data.dataOrNull(),
                transitionSpec = {
                    fadeIn().togetherWith(fadeOut())
                }
            ) { data ->
                QrCodeOrLoading(
                    data = data,
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            NumberedListOrganism(
                modifier = Modifier.fillMaxSize(),
                items = persistentListOf(
                    AnnotatedString(stringResource(R.string.screen_link_new_device_mobile_step1, appName)),
                    annotatedTextWithBold(
                        text = stringResource(
                            id = R.string.screen_link_new_device_mobile_step2,
                            stringResource(R.string.screen_link_new_device_mobile_step2_action),
                        ),
                        boldText = stringResource(R.string.screen_link_new_device_mobile_step2_action)
                    ),
                    AnnotatedString(stringResource(R.string.screen_link_new_device_mobile_step3)),
                )
            )
        }
    }
}

@Composable
private fun QrCodeOrLoading(
    data: String?,
    modifier: Modifier = Modifier,
) {
    if (data == null) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    } else {
        QrCodeImage(
            modifier = modifier,
            data = data,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun ShowQrCodeViewPreview(
    @PreviewParameter(ShowQrCodeStateProvider::class) state: ShowQrCodeState,
) = ElementPreview {
    ShowQrCodeView(
        state = state,
        onBackClick = { },
    )
}
