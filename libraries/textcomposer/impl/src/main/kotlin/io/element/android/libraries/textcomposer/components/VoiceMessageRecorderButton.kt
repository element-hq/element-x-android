/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.textcomposer.model.VoiceMessageRecorderEvent

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
        imageVector = CompoundIcons.MicOn(),
        // Note: accessibility is managed in TextComposer.
        contentDescription = null,
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
        // Note: accessibility is managed in TextComposer.
        contentDescription = null,
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
