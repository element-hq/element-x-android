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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.textcomposer.model.VoiceMessageRecorderEvent
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun VoiceMessageRecorderButton(
    isRecording: Boolean,
    onEvent: (VoiceMessageRecorderEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalHapticFeedback.current

    val performHapticFeedback = {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    if (isRecording) {
        StopButton(
            modifier = modifier,
            onClick = {
                performHapticFeedback()
                onEvent(VoiceMessageRecorderEvent.Stop)
            }
        )
    } else {
        StartButton(
            modifier = modifier,
            onClick = {
                performHapticFeedback()
                onEvent(VoiceMessageRecorderEvent.Start)
            }
        )
    }
}

@Composable
private fun StartButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = IconButton(
    modifier = modifier.size(48.dp),
    onClick = onClick,
) {
    Icon(
        modifier = Modifier.size(24.dp),
        imageVector = CompoundIcons.MicOnOutline,
        contentDescription = stringResource(CommonStrings.a11y_voice_message_record),
        tint = ElementTheme.colors.iconSecondary,
    )
}

@Composable
private fun StopButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = IconButton(
    modifier = modifier
        .size(48.dp),
    onClick = onClick,
) {
    Box(
        Modifier
            .size(36.dp)
            .background(
                color = ElementTheme.colors.bgActionPrimaryRest,
                shape = CircleShape,
            )
    )
    Icon(
        modifier = Modifier.size(24.dp),
        resourceId = CommonDrawables.ic_stop,
        contentDescription = stringResource(CommonStrings.a11y_voice_message_stop_recording),
        tint = ElementTheme.colors.iconOnSolidPrimary,
    )
}

@PreviewsDayNight
@Composable
internal fun VoiceMessageRecorderButtonPreview() = ElementPreview {
    Row {
        VoiceMessageRecorderButton(
            isRecording = false,
            onEvent = {},
        )
        VoiceMessageRecorderButton(
            isRecording = true,
            onEvent = {},
        )
    }
}
