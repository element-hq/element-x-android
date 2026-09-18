/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.circlemessages.composer

import android.graphics.Bitmap
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.api.timeline.circlemessages.composer.CircleMessageComposerEvent
import io.element.android.features.messages.api.timeline.circlemessages.composer.CircleMessageComposerState
import io.element.android.features.messages.api.timeline.circlemessages.composer.aCircleMessageComposerState
import io.element.android.libraries.androidutils.bitmap.blur
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.max

private const val CAMERA_FLIP_DRAG_THRESHOLD_PX = 36f
private const val CAMERA_ZOOM_DRAG_SPAN_PX = 220f

private enum class CircleGestureAxis {
    Horizontal,
    Vertical,
    Pinch,
}

@Composable
fun CircleMessageRecordingOverlay(
    state: CircleMessageComposerState,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val switchCameraLabel = stringResource(CommonStrings.a11y_circle_message_switch_camera)
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var freezeFrame by remember { mutableStateOf<Bitmap?>(null) }
    var linearZoom by remember { mutableFloatStateOf(0f) }

    fun flipCamera() {
        previewView?.bitmap?.let { bitmap ->
            freezeFrame = bitmap.blur(radius = 24f)
        }
        linearZoom = 0f
        state.eventSink(CircleMessageComposerEvent.SetLinearZoom(0f))
        state.eventSink(CircleMessageComposerEvent.FlipCamera)
    }

    fun updateZoom(zoom: Float) {
        val coerced = zoom.coerceIn(0f, 1f)
        linearZoom = coerced
        state.eventSink(CircleMessageComposerEvent.SetLinearZoom(coerced))
    }

    LaunchedEffect(state.isSwitchingCamera, freezeFrame) {
        if (freezeFrame == null || state.isSwitchingCamera) return@LaunchedEffect
        delay(250)
        freezeFrame = null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(ElementTheme.colors.bgSubtlePrimary)
                .semantics {
                    contentDescription = switchCameraLabel
                    onClick(label = switchCameraLabel) {
                        flipCamera()
                        true
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            if (LocalInspectionMode.current) {
                Text(
                    text = "Camera",
                    color = ElementTheme.colors.textPrimary,
                )
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        PreviewView(context).also { view ->
                            previewView = view
                            view.scaleX = if (state.useFrontCamera) -1f else 1f
                            state.eventSink(
                                CircleMessageComposerEvent.BindPreview(
                                    previewView = view,
                                    lifecycleOwner = lifecycleOwner,
                                )
                            )
                        }
                    },
                    update = { view ->
                        previewView = view
                        view.scaleX = if (state.useFrontCamera) -1f else 1f
                    },
                )
            }
            freezeFrame?.let { frame ->
                Image(
                    bitmap = frame.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            val touchSlop = viewConfiguration.touchSlop
                            val flipThreshold = max(CAMERA_FLIP_DRAG_THRESHOLD_PX, touchSlop * 1.5f)
                            var totalX = 0f
                            var totalY = 0f
                            var pinchScale = 1f
                            var axis: CircleGestureAxis? = null
                            var flipped = false
                            val startZoom = linearZoom
                            do {
                                val event = awaitPointerEvent()
                                val pressedCount = event.changes.count { it.pressed }
                                if (pressedCount >= 2) {
                                    axis = CircleGestureAxis.Pinch
                                    pinchScale *= event.calculateZoom()
                                    updateZoom(startZoom + (pinchScale - 1f))
                                    event.changes.forEach { it.consume() }
                                } else {
                                    val change = event.changes.first()
                                    val dx = change.position.x - change.previousPosition.x
                                    val dy = change.position.y - change.previousPosition.y
                                    totalX += dx
                                    totalY += dy
                                    change.consume()
                                    if (axis == null && (abs(totalX) > touchSlop || abs(totalY) > touchSlop)) {
                                        axis = if (abs(totalX) > abs(totalY)) {
                                            CircleGestureAxis.Horizontal
                                        } else {
                                            CircleGestureAxis.Vertical
                                        }
                                    }
                                    when (axis) {
                                        CircleGestureAxis.Horizontal -> {
                                            if (!flipped && abs(totalX) >= flipThreshold) {
                                                flipped = true
                                                flipCamera()
                                            }
                                        }
                                        CircleGestureAxis.Vertical -> {
                                            updateZoom(startZoom + totalY / CAMERA_ZOOM_DRAG_SPAN_PX)
                                        }
                                        else -> Unit
                                    }
                                }
                            } while (event.changes.any { it.pressed })
                        }
                    },
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun CircleMessageRecordingOverlayPreview() = ElementPreview {
    CircleMessageRecordingOverlay(
        state = aCircleMessageComposerState(isRecording = true),
    )
}
