/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.R
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * Touch holds to transmit; accessibility and keyboard activation toggle start/stop.
 * Release the button's transmit request when the gesture is cancelled, the screen pauses,
 * or the control leaves composition. This local state represents a request, not a granted floor.
 */
@Composable
internal fun PushToTalkButton(
    onButtonDown: () -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val currentOnButtonDown by rememberUpdatedState(onButtonDown)
    val currentOnRelease by rememberUpdatedState(onRelease)
    val label = stringResource(CommonStrings.a11y_ptt_button)
    val actionLabel = stringResource(if (isPressed) CommonStrings.a11y_ptt_stop else CommonStrings.a11y_ptt_start)

    fun stop() {
        if (isPressed) {
            isPressed = false
            currentOnRelease()
        }
    }

    fun start() {
        isPressed = true
        currentOnButtonDown()
    }

    fun toggle() {
        if (isPressed) stop() else start()
    }

    DisposableEffect(Unit) {
        onDispose { stop() }
    }
    OnLifecycleEvent { _, event ->
        if (event == Lifecycle.Event.ON_PAUSE) stop()
    }

    Box(
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clip(CircleShape)
            .background(if (isPressed) ElementTheme.colors.iconAccentPrimary else ElementTheme.colors.iconPrimary)
            .semantics {
                contentDescription = label
                role = Role.Button
                onClick(label = actionLabel) {
                    toggle()
                    true
                }
            }
            .onKeyEvent { event ->
                when (event.key) {
                    Key.Enter, Key.NumPadEnter, Key.Spacebar, Key.DirectionCenter -> {
                        if (event.type == KeyEventType.KeyUp) toggle()
                        true
                    }
                    Key.Escape -> {
                        if (event.type == KeyEventType.KeyUp) stop()
                        true
                    }
                    else -> false
                }
            }
            .focusable()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        if (isPressed) {
                            stop()
                        } else {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            try {
                                start()
                                tryAwaitRelease()
                            } finally {
                                stop()
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(26.dp),
            imageVector = ImageVector.vectorResource(R.drawable.ic_ptt_solid),
            contentDescription = null,
            tint = ElementTheme.colors.iconOnSolidPrimary,
        )
    }
}
