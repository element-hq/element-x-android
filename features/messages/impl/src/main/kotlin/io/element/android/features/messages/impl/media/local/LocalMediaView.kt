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

package io.element.android.features.messages.impl.media.local

import android.annotation.SuppressLint
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun LocalMediaView(
    localMedia: LocalMedia,
    modifier: Modifier = Modifier
) {
    when {
        MimeTypes.isImage(localMedia.mimeType) -> MediaImageView(
            localMedia = localMedia,
            modifier = modifier
        )
        MimeTypes.isVideo(localMedia.mimeType) -> MediaVideoView(
            localMedia = localMedia,
            modifier = modifier
        )
        else -> Unit
    }
}

@Composable
private fun MediaImageView(
    localMedia: LocalMedia,
    modifier: Modifier = Modifier,
) {
    val zoomableState = rememberZoomableState(
        zoomSpec = ZoomSpec(maxZoomFactor = 3f)
    )
    ZoomableAsyncImage(
        modifier = modifier.fillMaxSize(),
        state = rememberZoomableImageState(zoomableState),
        model = localMedia.model,
        contentDescription = "Image",
        contentScale = ContentScale.Fit,
    )
}

@UnstableApi
@Composable
fun MediaVideoView(
    localMedia: LocalMedia,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
            .apply {
                this.playWhenReady = true
                this.prepare()
            }
    }
    LaunchedEffect(localMedia.uri) {
        val mediaItem = MediaItem.fromUri(localMedia.uri)
        exoPlayer.setMediaItem(mediaItem)
    }

    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                controllerShowTimeoutMs = 3000
            }
        },
        modifier = modifier.fillMaxSize()
    )

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> exoPlayer.play()
            Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
            Lifecycle.Event.ON_DESTROY -> exoPlayer.release()
            else -> Unit
        }
    }
}
