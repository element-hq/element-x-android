/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.viewer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.androidutils.system.areAnimationsEnabled
import kotlinx.coroutines.delay
import me.saket.telephoto.ExperimentalTelephotoApi
import me.saket.telephoto.flick.FlickToDismiss
import me.saket.telephoto.flick.FlickToDismissState
import me.saket.telephoto.flick.rememberFlickToDismissState
import kotlin.time.Duration

@OptIn(ExperimentalTelephotoApi::class)
@Composable
fun MediaViewerFlickToDismiss(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onDragging: () -> Unit = {},
    onResetting: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    val flickState = rememberFlickToDismissState(dismissThresholdRatio = 0.1f, rotateOnDrag = false)
    val context = LocalContext.current
    DismissFlickEffects(
        flickState = flickState,
        onDismissing = { animationDuration ->
            // Only add the delay if an animation should be played, otherwise `onDismiss` will never be called
            if (context.areAnimationsEnabled()) {
                delay(animationDuration / 3)
            }
            onDismiss()
        },
        onDragging = onDragging,
        onResetting = onResetting,
    )
    FlickToDismiss(
        state = flickState,
        modifier = modifier.background(backgroundColorFor(flickState)),
        content = content,
    )
}

@Composable
private fun DismissFlickEffects(
    flickState: FlickToDismissState,
    onDismissing: suspend (Duration) -> Unit,
    onDragging: suspend () -> Unit,
    onResetting: suspend () -> Unit,
) {
    val currentOnDismissing by rememberUpdatedState(onDismissing)
    val currentOnDragging by rememberUpdatedState(onDragging)
    val currentOnResetting by rememberUpdatedState(onResetting)

    when (val gestureState = flickState.gestureState) {
        is FlickToDismissState.GestureState.Dismissing -> {
            LaunchedEffect(Unit) {
                currentOnDismissing(gestureState.animationDuration)
            }
        }
        is FlickToDismissState.GestureState.Dragging -> {
            LaunchedEffect(Unit) {
                currentOnDragging()
            }
        }
        is FlickToDismissState.GestureState.Resetting -> {
            LaunchedEffect(Unit) {
                currentOnResetting()
            }
        }
        else -> Unit
    }
}

@Composable
private fun backgroundColorFor(flickState: FlickToDismissState): Color {
    val animatedAlpha by animateFloatAsState(
        targetValue = when (flickState.gestureState) {
            is FlickToDismissState.GestureState.Dismissed,
            is FlickToDismissState.GestureState.Dismissing -> 0f
            is FlickToDismissState.GestureState.Dragging,
            is FlickToDismissState.GestureState.Idle,
            is FlickToDismissState.GestureState.Resetting -> 1f - flickState.offsetFraction
        },
        label = "Background alpha",
    )
    return ElementTheme.colors.bgCanvasDefault.copy(alpha = animatedAlpha)
}
