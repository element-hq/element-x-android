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
import io.element.android.features.messages.impl.media.local.LocalMediaActions
import io.element.android.features.messages.impl.media.local.LocalMediaFactory
import io.element.android.features.messages.impl.media.local.createFromMediaFile
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.utils.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.SnackbarMessage
import io.element.android.libraries.designsystem.utils.handleSnackbarMessage
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.media.MediaFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import io.element.android.libraries.ui.strings.R as StringR

class MediaViewerPresenter @AssistedInject constructor(
    @Assisted private val inputs: MediaViewerNode.Inputs,
    private val localMediaFactory: LocalMediaFactory,
    private val mediaLoader: MatrixMediaLoader,
    private val mediaActionsHandler: LocalMediaActions,
    private val snackbarDispatcher: SnackbarDispatcher,
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
        val snackbarMessage = handleSnackbarMessage(snackbarDispatcher)
        DisposableEffect(loadMediaTrigger) {
            coroutineScope.downloadMedia(mediaFile, localMedia)
            onDispose {
                mediaFile.value?.close()
            }
        }

        fun handleEvents(mediaViewerEvents: MediaViewerEvents) {
            when (mediaViewerEvents) {
                MediaViewerEvents.RetryLoading -> loadMediaTrigger++
                MediaViewerEvents.ClearLoadingError -> localMedia.value = Async.Uninitialized
                MediaViewerEvents.SaveOnDisk -> coroutineScope.saveOnDisk(localMedia.value)
                MediaViewerEvents.Share -> coroutineScope.share(localMedia.value)
            }
        }

        return MediaViewerState(
            name = inputs.name,
            mimeType = inputs.mimeType,
            thumbnailSource = inputs.thumbnailSource,
            downloadedMedia = localMedia.value,
            snackbarMessage = snackbarMessage,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.downloadMedia(mediaFile: MutableState<MediaFile?>, localMedia: MutableState<Async<LocalMedia>>) = launch {
        localMedia.value = Async.Loading()
        mediaLoader.downloadMediaFile(
            source = inputs.mediaSource,
            mimeType = inputs.mimeType,
            body = inputs.name
        )
            .onSuccess {
                mediaFile.value = it
            }.mapCatching { mediaFile ->
                localMediaFactory.createFromMediaFile(
                    mediaFile = mediaFile,
                    mimeType = inputs.mimeType,
                    name = inputs.name
                )
            }.onSuccess {
                localMedia.value = Async.Success(it)
            }.onFailure {
                localMedia.value = Async.Failure(it)
            }
    }

    private fun CoroutineScope.saveOnDisk(localMedia: Async<LocalMedia>) = launch {
        when (localMedia) {
            is Async.Success -> {
                mediaActionsHandler.saveOnDisk(localMedia.state)
                    .onSuccess {
                        val snackbarMessage = SnackbarMessage(StringR.string.common_file_saved_on_disk_android)
                        snackbarDispatcher.post(snackbarMessage)
                    }
            }
            else -> Unit
        }
    }

    private fun CoroutineScope.share(localMedia: Async<LocalMedia>) = launch {
        when (localMedia) {
            is Async.Success -> mediaActionsHandler.share(localMedia.state)
            else -> Unit
        }
    }
}



