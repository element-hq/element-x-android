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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.messages.impl.media.viewer.model.MediaContentUiModel
import io.element.android.libraries.designsystem.components.blurhash.BlurHashAsyncImage
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight

@Composable
fun MediaViewerView(
    state: MediaViewerState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        when (state.mediaContent) {
            is MediaContentUiModel.Image -> MediaImageViewer(state.mediaContent)
            is MediaContentUiModel.Video -> MediaVideoViewer(state.mediaContent)
        }
    }
}

@Composable
private fun MediaImageViewer(
    image: MediaContentUiModel.Image,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        BlurHashAsyncImage(
            blurHash = image.blurhash,
            model = image.mediaRequestData,
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun MediaVideoViewer(
    video: MediaContentUiModel.Video,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

    }
}

@Preview
@Composable
fun MediaViewerViewLightPreview(@PreviewParameter(MediaViewerStateProvider::class) state: MediaViewerState) =
    ElementPreviewLight { ContentToPreview(state) }

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
