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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.messages.impl.media.local.LocalMedia
import io.element.android.features.messages.impl.media.local.LocalMediaFactory
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.media.MediaFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MediaViewerPresenter @AssistedInject constructor(
    @Assisted private val inputs: MediaViewerNode.Inputs,
    private val localMediaFactory: LocalMediaFactory,
    private val mediaLoader: MatrixMediaLoader,
) : Presenter<MediaViewerState> {

    @AssistedFactory
    interface Factory {
        fun create(inputs: MediaViewerNode.Inputs): MediaViewerPresenter
    }

    @Composable
    override fun present(): MediaViewerState {
        val coroutineScope = rememberCoroutineScope()
        var loadMediaTrigger by remember { mutableStateOf(0) }
        val mediaFile: MutableState<MediaFile?> = remember {
            mutableStateOf(null)
        }
        val localMedia: MutableState<Async<LocalMedia>> = remember {
            mutableStateOf(Async.Uninitialized)
        }
        DisposableEffect(loadMediaTrigger) {
            coroutineScope.loadMedia(mediaFile, localMedia)
            onDispose {
                mediaFile.value?.close()
            }
        }

        fun handleEvents(mediaViewerEvents: MediaViewerEvents) {
            when (mediaViewerEvents) {
                MediaViewerEvents.RetryLoading -> loadMediaTrigger++
                MediaViewerEvents.ClearLoadingError -> localMedia.value = Async.Uninitialized
            }
        }

        return MediaViewerState(
            name = inputs.name,
            mimeType = inputs.mimeType,
            thumbnailSource = inputs.thumbnailSource,
            downloadedMedia = localMedia.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.loadMedia(mediaFile: MutableState<MediaFile?>, localMedia: MutableState<Async<LocalMedia>>) = launch {
        localMedia.value = Async.Loading()
        mediaLoader.loadMediaFile(inputs.mediaSource, inputs.mimeType)
            .onSuccess {
                mediaFile.value = it
            }.mapCatching {
                localMediaFactory.createFromMediaFile(it, inputs.mimeType)
            }.onSuccess {
                localMedia.value = Async.Success(it)
            }.onFailure {
                localMedia.value = Async.Failure(it)
            }
    }
}
