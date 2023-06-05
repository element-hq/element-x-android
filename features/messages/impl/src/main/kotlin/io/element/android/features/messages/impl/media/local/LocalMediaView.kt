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
import android.net.Uri
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Attachment
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import io.element.android.features.messages.impl.media.helper.formatFileExtensionAndSize
import io.element.android.features.messages.impl.media.local.exoplayer.ExoPlayerWrapper
import io.element.android.features.messages.impl.media.local.pdf.PdfViewer
import io.element.android.features.messages.impl.media.local.pdf.rememberPdfViewerState
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeImage
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeVideo
import io.element.android.libraries.designsystem.R
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.ZoomableState
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun LocalMediaView(
    localMedia: LocalMedia?,
    modifier: Modifier = Modifier,
    info: MediaInfo? = localMedia?.info,
    onReady: () -> Unit = {},
) {
    val zoomableState = rememberZoomableState(
        zoomSpec = ZoomSpec(maxZoomFactor = 5f)
    )
    val mimeType = info?.mimeType
    when {
        mimeType.isMimeTypeImage() -> MediaImageView(
            localMedia = localMedia,
            zoomableState = zoomableState,
            onReady = onReady,
            modifier = modifier
        )
        mimeType.isMimeTypeVideo() -> MediaVideoView(
            localMedia = localMedia,
            onReady = onReady,
            modifier = modifier
        )
        mimeType == MimeTypes.Pdf -> MediaPDFView(
            localMedia = localMedia,
            zoomableState = zoomableState,
            onReady = onReady,
            modifier = modifier
        )
        else -> MediaFileView(
            uri = localMedia?.uri,
            info = info,
            onReady = onReady,
            modifier = modifier
        )
    }
}

@Composable
private fun MediaImageView(
    localMedia: LocalMedia?,
    zoomableState: ZoomableState,
    onReady: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) {
        Image(
            painter = painterResource(id = R.drawable.sample_background),
            modifier = modifier.fillMaxSize(),
            contentDescription = null,
        )
    } else {
        val zoomableImageState = rememberZoomableImageState(zoomableState)
        LaunchedEffect(zoomableImageState.isImageDisplayed) {
            if (zoomableImageState.isImageDisplayed) {
                onReady()
            }
        }
        ZoomableAsyncImage(
            modifier = modifier.fillMaxSize(),
            state = zoomableImageState,
            model = localMedia?.uri,
            contentDescription = "Image",
            contentScale = ContentScale.Fit,
        )
    }
}

@UnstableApi
@Composable
fun MediaVideoView(
    localMedia: LocalMedia?,
    onReady: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val playerListener = object : Player.Listener {
        override fun onRenderedFirstFrame() {
            onReady()
        }
    }
    val exoPlayer = remember {
        ExoPlayerWrapper.create(context)
            .apply {
                addListener(playerListener)
                this.prepare()
            }
    }
    if (localMedia?.uri != null) {
        LaunchedEffect(localMedia.uri) {
            val mediaItem = MediaItem.fromUri(localMedia.uri)
            exoPlayer.setMediaItem(mediaItem)
        }
    } else {
        exoPlayer.setMediaItems(emptyList())
    }
    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                setShowPreviousButton(false)
                setShowNextButton(false)
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
            Lifecycle.Event.ON_DESTROY -> {
                exoPlayer.release()
                exoPlayer.removeListener(playerListener)
            }
            else -> Unit
        }
    }
}

@Composable
fun MediaPDFView(
    localMedia: LocalMedia?,
    zoomableState: ZoomableState,
    onReady: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pdfViewerState = rememberPdfViewerState(
        model = localMedia?.uri,
        zoomableState = zoomableState
    )
    LaunchedEffect(pdfViewerState.isLoaded) {
        if (pdfViewerState.isLoaded) {
            onReady()
        }
    }
    PdfViewer(pdfViewerState = pdfViewerState, modifier = modifier)
}

@Composable
fun MediaFileView(
    uri: Uri?,
    info: MediaInfo?,
    onReady: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        if(uri != null) {
            onReady()
        }
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Attachment,
                    contentDescription = "OpenFile",
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .size(32.dp)
                        .rotate(-45f),
                )
            }
            if(info == null) return
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = info.name,
                maxLines = 2,
                fontSize = 16.sp,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatFileExtensionAndSize(info.name, info.formattedFileSize),
                fontSize = 14.sp,
            )
        }
    }
}
