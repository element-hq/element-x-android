/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.R
import io.element.android.libraries.designsystem.theme.components.Icon

/**
 * Press-and-hold "push to talk" button that takes the microphone's slot in the composer when the
 * user has joined a PTT session. Fires [onPress] on press and [onRelease] on release/cancel.
 *
 * NOTE: the composer has no press-and-hold affordance today (the voice-message mic is tap-toggle),
 * so the gesture is built here. Actual audio transmit is wired in a later stage (Element Call mic
 * control); for now [onPress]/[onRelease] give the caller the press window plus haptic feedback.
 */
@Composable
internal fun PushToTalkButton(
    onPress: () -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            // TODO localise once the prototype graduates.
            .clearAndSetSemantics { contentDescription = "Push to talk. Hold to transmit." }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onPress()
                        tryAwaitRelease()
                        isPressed = false
                        onRelease()
                    }
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier
                .clip(CircleShape)
                .size(36.dp)
                .background(
                    if (isPressed) ElementTheme.colors.iconAccentPrimary else ElementTheme.colors.iconPrimary
                )
                .padding(8.dp),
            imageVector = ImageVector.vectorResource(R.drawable.ic_ptt),
            contentDescription = null,
            tint = ElementTheme.colors.iconOnSolidPrimary,
        )
    }
}
