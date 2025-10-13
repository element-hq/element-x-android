/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.floatingvideo.ui

import android.net.Uri
import android.view.View
import android.view.WindowManager
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.getScreenWidth
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.getVideoUriFromMediaSource
import io.element.android.libraries.mediaviewer.impl.viewer.MediaViewerPageData
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun FloatingVideoOverlay(
    onClose: () -> Unit,
    onToggleFullScreen: (Float) -> Unit,
    currentVideoData : MediaViewerPageData.MediaViewerData?,
    isMaximized : Boolean,
    windowManager : WindowManager?,
    windowLayoutParams : WindowManager.LayoutParams,
    floatingView : View?
) {
    var currentAspectRatio by remember { mutableFloatStateOf(16f / 9f) }
    val videoViewRef = remember { mutableStateOf<VideoView?>(null) }



    var resolvedUri: Uri = Uri.EMPTY
    currentVideoData?.let { data ->
        resolvedUri = when (val downloadedState = data.downloadedMedia.value) {
            is AsyncData.Success -> downloadedState.data.uri
            else -> data.mediaSource.getVideoUriFromMediaSource()
        }
    }

    // Function to update window size directly
    fun updateWindowSize(aspectRatio: Float) {
        val widthFrac = if (aspectRatio > 1f) 0.6f else 0.3f
        val width = if (isMaximized) {
            (windowManager.getScreenWidth() * widthFrac).toInt()
        } else {
            (windowManager.getScreenWidth() * 0.9f).toInt()
        }
        val height = (width / aspectRatio).toInt()


        windowLayoutParams.width = width
        windowLayoutParams.height = height
        windowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        windowManager?.updateViewLayout(floatingView, windowLayoutParams)
    }

    // Initial window size (16:9)
    LaunchedEffect(Unit) {
        updateWindowSize(16f / 9f)
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black).pointerInput(Unit) {
                var dragStarted = false
                detectTapGestures(
                    onPress = {
                        dragStarted = false
                    },
                    onTap = {
                        if (!dragStarted) {
                            videoViewRef.value?.let { video ->
                                if (video.isPlaying) {
                                    video.pause()
                                } else {
                                    video.start()
                                }
                            }
                        }
                    }
                )
            }
    ) {
        // Video layer
        AndroidView(
            factory = { context ->
                VideoView(context).apply {
                    videoViewRef.value = this
                    setVideoURI(resolvedUri)
                    setOnPreparedListener { mp ->
                        val videoWidth = mp.videoWidth
                        val videoHeight = mp.videoHeight

                        if (videoWidth > 0 && videoHeight > 0) {
                            val newAspectRatio = videoWidth.toFloat() / videoHeight

                            // Store the aspect ratio and update window size
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                currentAspectRatio = newAspectRatio
                                updateWindowSize(newAspectRatio)
                            }
                        }
                        start()
                    }

                }
            },
            update = { videoView ->
                if (resolvedUri != Uri.EMPTY && videoView.currentPosition == 0) {
                    videoView.setVideoURI(resolvedUri)
                }
            },
            modifier = Modifier
                .fillMaxSize()

        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newX = windowLayoutParams.x + dragAmount.x.toInt()
                        val newY = windowLayoutParams.y + dragAmount.y.toInt()
                        windowLayoutParams.x = newX
                        windowLayoutParams.y = newY
                        windowManager?.updateViewLayout(floatingView, windowLayoutParams)
                    }
                }
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                           Color.Black.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    )
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    onToggleFullScreen ( currentAspectRatio )
//                    updateWindowSize(currentAspectRatio)
                },
                //it seems the CompoundIcons.Expand() is bigger than the CompoundIcons.Close(),
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = CompoundIcons.Expand(),
                    //action full screen needs to be added to CommonsString
                    contentDescription = stringResource(CommonStrings.a11y_expand_message_text_field),
                    tint = Color.White,
                    modifier = Modifier.padding(4.dp)
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = CompoundIcons.Close(),
                    contentDescription = stringResource(CommonStrings.action_close),
                    tint = Color.White
                )
            }
        }
    }
}
