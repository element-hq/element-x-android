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

package io.element.android.libraries.textcomposer.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.applyScaleUp
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.textcomposer.utils.PressState
import io.element.android.libraries.textcomposer.utils.PressStateEffects
import io.element.android.libraries.textcomposer.utils.rememberPressState
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.launch

@Composable
internal fun RecordButton(
    modifier: Modifier = Modifier,
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
        },
    )

    RecordButtonView(
        isPressed = pressState.value is PressState.Pressing,
        modifier = modifier
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

@Composable
private fun RecordButtonView(
    isPressed: Boolean,
    modifier: Modifier = Modifier,
) {
    IconButton(
        modifier = modifier
            .size(48.dp.applyScaleUp()),
        onClick = {},
    ) {
        Icon(
            modifier = Modifier.size(24.dp.applyScaleUp()),
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

@PreviewsDayNight
@Composable
internal fun RecordButtonPreview() = ElementPreview {
    Row {
        RecordButtonView(isPressed = false)
        RecordButtonView(isPressed = true)
    }
}

