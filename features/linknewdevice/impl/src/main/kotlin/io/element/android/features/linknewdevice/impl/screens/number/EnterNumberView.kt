/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.linknewdevice.impl.screens.number

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.linknewdevice.impl.R
import io.element.android.features.linknewdevice.impl.screens.number.component.NumberTextField
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.linknewdevice.ErrorType
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * Form to enter number:
 * https://www.figma.com/design/pDlJZGBsri47FNTXMnEdXB/Compound-Android-Templates?node-id=2076-81604
 */
@Composable
fun EnterNumberView(
    state: EnterNumberState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowStepPage(
        onBackClick = onBackClick,
        title = stringResource(R.string.screen_link_new_device_enter_number_title),
        subTitle = stringResource(R.string.screen_link_new_device_enter_number_subtitle),
        iconStyle = BigIcon.Style.Default(CompoundIcons.Computer()),
        modifier = modifier,
        isScrollable = true,
        buttons = {
            Button(
                text = stringResource(CommonStrings.action_continue),
                onClick = { state.eventSink(EnterNumberEvent.Continue) },
                enabled = state.isContinueButtonEnabled,
                showProgress = state.sendingCode.isLoading(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.screen_link_new_device_enter_number_notice),
                textAlign = TextAlign.Center,
                style = ElementTheme.typography.fontBodyMdRegular,
                color = ElementTheme.colors.textPrimary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            NumberTextField(
                number = state.numberEntry,
                onValueChange = { state.eventSink(EnterNumberEvent.UpdateNumber(it)) },
                onDone = {
                    if (state.isContinueButtonEnabled) {
                        state.eventSink(EnterNumberEvent.Continue)
                    }
                },
            )
            val failure = state.sendingCode.errorOrNull()
            if (failure != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(14.dp),
                        imageVector = CompoundIcons.ErrorSolid(),
                        contentDescription = null,
                        tint = ElementTheme.colors.iconCriticalPrimary,
                    )
                    val errorMessage = when (failure) {
                        is ErrorType.InvalidCheckCode -> stringResource(R.string.screen_link_new_device_enter_number_error_numbers_do_not_match)
                        else -> failure.message ?: stringResource(CommonStrings.error_unknown)
                    }
                    Text(
                        text = errorMessage,
                        style = ElementTheme.typography.fontBodySmRegular,
                        color = ElementTheme.colors.textCriticalPrimary,
                    )
                }
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun EnterNumberViewPreview(
    @PreviewParameter(EnterNumberStatePreviewParam::class) state: EnterNumberState,
) = ElementPreview {
    EnterNumberView(
        state = state,
        onBackClick = { },
    )
}
