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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton

@Composable
internal fun VoiceMessageRecorderButtonIcon(
    isRecording: Boolean,
    modifier: Modifier = Modifier,
) {
    if (isRecording) {
        StopButton(modifier)
    } else {
        StartButton(modifier)
    }
}

@Composable
private fun StartButton(
    modifier: Modifier = Modifier,
) {
    Icon(
        modifier = modifier.size(24.dp),
        imageVector = CompoundIcons.MicOnSolid(),
        // Note: accessibility is managed in TextComposer.
        contentDescription = null,
        tint = ElementTheme.colors.iconSecondary,
    )
}

@Composable
private fun StopButton(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(36.dp)
            .background(
                color = ElementTheme.colors.bgActionPrimaryRest,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = CompoundIcons.StopSolid(),
            // Note: accessibility is managed in TextComposer.
            contentDescription = null,
            tint = ElementTheme.colors.iconOnSolidPrimary,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun VoiceMessageRecorderButtonIconPreview() = ElementPreview {
    Row {
        IconButton(onClick = {}) {
            VoiceMessageRecorderButtonIcon(
                isRecording = false,
            )
        }
        IconButton(onClick = {}) {
            VoiceMessageRecorderButtonIcon(
                isRecording = true,
            )
        }
    }
}
