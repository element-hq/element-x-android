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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import io.element.android.features.messages.impl.media.local.exoplayer.ExoPlayerWrapper
import io.element.android.features.messages.impl.media.local.pdf.ParcelFileDescriptorFactory
import io.element.android.features.messages.impl.media.local.pdf.PdfPage
import io.element.android.features.messages.impl.media.local.pdf.PdfRendererManager
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeImage
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeVideo
import io.element.android.libraries.designsystem.R
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.ZoomableState
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun LocalMediaView(
    localMedia: LocalMedia?,
    modifier: Modifier = Modifier,
    mimeType: String? = localMedia?.mimeType,
    onReady: () -> Unit = {},
) {
    val zoomableState = rememberZoomableState(
        zoomSpec = ZoomSpec(maxZoomFactor = 3f)
    )
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
        mimeType == MimeTypes.Pdf -> {
            MediaPDFView(
                localMedia = localMedia,
                zoomableState = zoomableState,
                onReady = onReady,
                modifier = modifier
            )
        }
        else -> Unit
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
            model = localMedia?.model,
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

@UnstableApi
@Composable
fun MediaPDFView(
    localMedia: LocalMedia?,
    zoomableState: ZoomableState,
    onReady: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.zoomable(zoomableState),
        contentAlignment = Alignment.Center
    ) {
        val maxWidth = this.maxWidth.dpToPx()
        val lazyListState = rememberLazyListState()
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        var pdfRendererManager by remember {
            mutableStateOf<PdfRendererManager?>(null)
        }
        DisposableEffect(localMedia) {
            ParcelFileDescriptorFactory(context).create(localMedia?.model)
                .onSuccess {
                    pdfRendererManager = PdfRendererManager(it, maxWidth, coroutineScope).apply {
                        open()
                    }
                    onReady()
                }
            onDispose {
                pdfRendererManager?.close()
            }
        }
        pdfRendererManager?.run {
            val pdfPages = pdfPages.collectAsState().value
            PdfPagesView(pdfPages.toImmutableList(), lazyListState)
        }
    }
}

@Composable
private fun PdfPagesView(
    pdfPages: ImmutableList<PdfPage>,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)

    ) {
        items(pdfPages.size) { index ->
            val pdfPage = pdfPages[index]
            PdfPageView(pdfPage)
        }
    }
}

@Composable
private fun PdfPageView(
    pdfPage: PdfPage,
    modifier: Modifier = Modifier,
) {
    val pdfPageState by pdfPage.stateFlow.collectAsState()
    DisposableEffect(pdfPage) {
        pdfPage.load()
        onDispose {
            pdfPage.close()
        }
    }
    when (val state = pdfPageState) {
        is PdfPage.State.Loaded -> {
            Image(
                bitmap = state.bitmap.asImageBitmap(),
                contentDescription = "Page ${pdfPage.pageIndex}",
                contentScale = ContentScale.FillWidth,
                modifier = modifier.fillMaxWidth()
            )
        }
        is PdfPage.State.Loading -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(state.height.pxToDp())
                    .background(color = Color.White)
            )
        }
    }
}

@Composable
private fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }

@Composable
private fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.roundToPx() }
