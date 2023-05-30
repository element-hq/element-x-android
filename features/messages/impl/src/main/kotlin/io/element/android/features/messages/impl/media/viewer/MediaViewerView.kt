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


package io.element.android.features.messages.impl.media.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import coil.compose.AsyncImage
import io.element.android.features.messages.impl.media.local.LocalMediaView
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.isLoading
import io.element.android.libraries.designsystem.components.dialogs.RetryDialog
import io.element.android.libraries.designsystem.modifiers.roundedBackground
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import kotlinx.coroutines.delay
import io.element.android.libraries.ui.strings.R as StringR

@Composable
fun MediaViewerView(
    state: MediaViewerState,
    modifier: Modifier = Modifier,
) {

    fun onRetry() {
        state.eventSink(MediaViewerEvents.RetryLoading)
    }

    fun onDismissError() {
        state.eventSink(MediaViewerEvents.ClearLoadingError)
    }

    var showProgress by remember {
        mutableStateOf(false)
    }

    // Trick to avoid showing progress indicator if the media is already on disk.
    // When sdk will expose download progress we'll be able to remove this.
    LaunchedEffect(state.downloadedMedia) {
        showProgress = false
        delay(100)
        if (state.downloadedMedia.isLoading()) {
            showProgress = true
        }
    }

    var showThumbnail by remember {
        mutableStateOf(true)
    }

    fun onMediaReady() {
        showThumbnail = false
    }

    Scaffold(modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentAlignment = Alignment.Center
        ) {
            if (state.downloadedMedia is Async.Failure) {
                ErrorView(
                    errorMessage = stringResource(id = StringR.string.error_unknown),
                    onRetry = ::onRetry,
                    onDismiss = ::onDismissError
                )
            }
            LocalMediaView(
                localMedia = state.downloadedMedia.dataOrNull(),
                mimeType = state.mimeType,
                onReady = ::onMediaReady
            )
            ThumbnailView(
                thumbnailSource = state.thumbnailSource,
                showThumbnail = showThumbnail,
                showProgress = showProgress,
            )
        }
    }
}

@Composable
private fun ThumbnailView(
    thumbnailSource: MediaSource?,
    showThumbnail: Boolean,
    showProgress: Boolean,
) {
    AnimatedVisibility(
        visible = showThumbnail,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val mediaRequestData = MediaRequestData(
                source = thumbnailSource,
                kind = MediaRequestData.Kind.Content
            )
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = mediaRequestData,
                alpha = 0.8f,
                contentScale = ContentScale.Fit,
                contentDescription = null,
            )
            if (showProgress) {
                Box(
                    modifier = Modifier.roundedBackground(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun ErrorView(
    errorMessage: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RetryDialog(
        modifier = modifier,
        content = errorMessage,
        onRetry = onRetry,
        onDismiss = onDismiss
    )
}

@Preview
@Composable
fun MediaViewerViewDarkPreview(@PreviewParameter(MediaViewerStateProvider::class) state: MediaViewerState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: MediaViewerState) {
    MediaViewerView(
        state = state,
    )
}
