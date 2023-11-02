/*
 * Copyright (c) 2023 New Vector Ltd
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

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.libraries.textcomposer.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TooltipState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.components.tooltip.ElementTooltipDefaults
import io.element.android.libraries.designsystem.components.tooltip.PlainTooltip
import io.element.android.libraries.designsystem.components.tooltip.TooltipBox
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.textcomposer.R
import io.element.android.libraries.textcomposer.utils.PressState
import io.element.android.libraries.textcomposer.utils.PressStateEffects
import io.element.android.libraries.textcomposer.utils.rememberPressState
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecordButton(
    modifier: Modifier = Modifier,
    initialTooltipIsVisible: Boolean = false,
    onPressStart: () -> Unit = {},
    onLongPressEnd: () -> Unit = {},
    onTap: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val pressState = rememberPressState()
    val hapticFeedback = LocalHapticFeedback.current

    val performHapticFeedback = {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val tooltipState = rememberTooltipState(
        initialIsVisible = initialTooltipIsVisible
    )

    PressStateEffects(
        pressState = pressState.value,
        onPressStart = {
            onPressStart()
            performHapticFeedback()
        },
        onLongPressEnd = {
            onLongPressEnd()
            performHapticFeedback()
        },
        onTap = {
            onTap()
            performHapticFeedback()
            coroutineScope.launch { tooltipState.show() }
        },
    )
    Box(modifier = modifier) {
        HoldToRecordTooltip(
            tooltipState = tooltipState,
            spacingBetweenTooltipAndAnchor = 0.dp, // Accounts for the 48.dp size of the record button
            anchor = {
                RecordButtonView(
                    isPressed = pressState.value is PressState.Pressing,
                    modifier = Modifier
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    coroutineScope.launch {
                                        when (event.type) {
                                            PointerEventType.Press -> pressState.press()
                                            PointerEventType.Release -> pressState.release()
                                        }
                                    }
                                }
                            }
                        }
                )
            }
        )
    }
}

@Composable
private fun RecordButtonView(
    isPressed: Boolean,
    modifier: Modifier = Modifier,
) {
    IconButton(
        modifier = modifier
            .size(48.dp),
        onClick = {},
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            resourceId = if (isPressed) {
                CommonDrawables.ic_compound_mic_on_solid
            } else {
                CommonDrawables.ic_compound_mic_on_outline
            },
            contentDescription = stringResource(CommonStrings.a11y_voice_message_record),
            tint = ElementTheme.colors.iconSecondary,
        )
    }
}

@Composable
private fun HoldToRecordTooltip(
    tooltipState: TooltipState,
    spacingBetweenTooltipAndAnchor: Dp,
    modifier: Modifier = Modifier,
    anchor: @Composable () -> Unit,
) {
    TooltipBox(
        positionProvider = ElementTooltipDefaults.rememberPlainTooltipPositionProvider(
            spacingBetweenTooltipAndAnchor = spacingBetweenTooltipAndAnchor,
        ),
        tooltip = {
            PlainTooltip {
                Text(
                    text = stringResource(R.string.screen_room_voice_message_tooltip),
                    color = ElementTheme.colors.textOnSolidPrimary,
                    style = ElementTheme.typography.fontBodySmMedium,
                )
            }
        },
        state = tooltipState,
        modifier = modifier,
        focusable = false,
        enableUserInput = false,
        content = anchor,
    )
}

@PreviewsDayNight
@Composable
internal fun RecordButtonPreview() = ElementPreview {
    Row {
        RecordButtonView(isPressed = false)
        RecordButtonView(isPressed = true)
    }
}

@PreviewsDayNight
@Composable
internal fun HoldToRecordTooltipPreview() = ElementPreview {
    Box(modifier = Modifier.fillMaxSize()) {
        RecordButton(
            modifier = Modifier.align(Alignment.BottomEnd),
            initialTooltipIsVisible = true,
        )
    }
}
