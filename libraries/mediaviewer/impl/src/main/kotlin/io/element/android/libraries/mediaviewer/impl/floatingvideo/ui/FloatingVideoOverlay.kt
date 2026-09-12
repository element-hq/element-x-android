/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.floatingvideo.ui

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.delay

@Composable
fun FloatingVideoOverlay(
    uri: Uri,
    startPositionMs: Long,
    onClose: () -> Unit,
    onToggleFullScreen: (Float) -> Unit,
    onCompleted: () -> Unit,
    updateAspectRatio: (Float) -> Unit,
    movePosition: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentAspectRatio by remember { mutableFloatStateOf(16f / 9f) }
    val videoViewRef = remember { mutableStateOf<VideoView?>(null) }
    var isPlaying by remember { mutableStateOf(true) }
    var showPlayPauseAffordance by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        updateAspectRatio(16f / 9f)
    }

    LaunchedEffect(isPlaying, showPlayPauseAffordance) {
        if (isPlaying && showPlayPauseAffordance) {
            delay(1_200)
            showPlayPauseAffordance = false
        }
    }

    fun togglePlayPause() {
        val video = videoViewRef.value ?: return
        if (video.isPlaying) {
            video.pause()
            isPlaying = false
        } else {
            video.start()
            isPlaying = true
        }
        showPlayPauseAffordance = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) { detectTapGestures(onTap = { togglePlayPause() }) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    movePosition(dragAmount.x.toInt(), dragAmount.y.toInt())
                }
            },
    ) {
        AndroidView(
            factory = { context ->
                VideoView(context).apply {
                    videoViewRef.value = this
                    setVideoURI(uri)
                    setOnPreparedListener { mp ->
                        if (mp.videoWidth > 0 && mp.videoHeight > 0) {
                            currentAspectRatio = mp.videoWidth.toFloat() / mp.videoHeight
                            updateAspectRatio(currentAspectRatio)
                        }
                        if (startPositionMs > 0L) seekTo(startPositionMs.toInt())
                        start()
                        isPlaying = true
                    }
                    setOnCompletionListener {
                        isPlaying = false
                        onCompleted()
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        if (!isPlaying || showPlayPauseAffordance) {
            IconButton(
                onClick = { togglePlayPause() },
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(56.dp)
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape),
            ) {
                Icon(
                    imageVector = if (isPlaying) CompoundIcons.PauseSolid() else CompoundIcons.PlaySolid(),
                    contentDescription = stringResource(
                        if (isPlaying) CommonStrings.a11y_pause else CommonStrings.a11y_play
                    ),
                    tint = Color.White,
                    modifier = Modifier.size(32.dp),
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent))
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = { onToggleFullScreen(currentAspectRatio) }, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = CompoundIcons.Expand(),
                    contentDescription = stringResource(CommonStrings.a11y_expand_message_text_field),
                    tint = Color.White,
                    modifier = Modifier.padding(4.dp),
                )
            }
            IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = CompoundIcons.Close(),
                    contentDescription = stringResource(CommonStrings.action_close),
                    tint = Color.White,
                )
            }
        }
    }
}
