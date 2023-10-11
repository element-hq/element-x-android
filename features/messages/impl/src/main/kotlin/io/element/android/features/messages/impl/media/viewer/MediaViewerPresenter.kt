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

import android.content.ActivityNotFoundException
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.designsystem.utils.snackbar.collectSnackbarMessageAsState
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.media.MediaFile
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import io.element.android.libraries.androidutils.R as UtilsR

class MediaViewerPresenter @AssistedInject constructor(
    @Assisted private val inputs: MediaViewerNode.Inputs,
    private val localMediaFactory: LocalMediaFactory,
    private val mediaLoader: MatrixMediaLoader,
    private val localMediaActions: LocalMediaActions,
    private val snackbarDispatcher: SnackbarDispatcher,
) : Presenter<MediaViewerState> {

    @AssistedFactory
    interface Factory {
        fun create(inputs: MediaViewerNode.Inputs): MediaViewerPresenter
    }

    @Composable
    override fun present(): MediaViewerState {
        val coroutineScope = rememberCoroutineScope()
        var loadMediaTrigger by remember { mutableIntStateOf(0) }
        val mediaFile: MutableState<MediaFile?> = remember {
            mutableStateOf(null)
        }
        val localMedia: MutableState<Async<LocalMedia>> = remember {
            mutableStateOf(Async.Uninitialized)
        }
        val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()
        localMediaActions.Configure()
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
                MediaViewerEvents.OpenWith -> coroutineScope.open(localMedia.value)
            }
        }

        return MediaViewerState(
            mediaInfo = inputs.mediaInfo,
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
            mimeType = inputs.mediaInfo.mimeType,
            body = inputs.mediaInfo.name
        )
            .onSuccess {
                mediaFile.value = it
            }
            .mapCatching { mediaFile ->
                localMediaFactory.createFromMediaFile(
                    mediaFile = mediaFile,
                    mediaInfo = inputs.mediaInfo
                )
            }
            .onSuccess {
                localMedia.value = Async.Success(it)
            }
            .onFailure {
                localMedia.value = Async.Failure(it)
            }
    }

    private fun CoroutineScope.saveOnDisk(localMedia: Async<LocalMedia>) = launch {
        if (localMedia is Async.Success) {
            localMediaActions.saveOnDisk(localMedia.data)
                .onSuccess {
                    val snackbarMessage = SnackbarMessage(CommonStrings.common_file_saved_on_disk_android)
                    snackbarDispatcher.post(snackbarMessage)
                }
                .onFailure {
                    val snackbarMessage = SnackbarMessage(mediaActionsError(it))
                    snackbarDispatcher.post(snackbarMessage)
                }
        } else Unit
    }

    private fun CoroutineScope.share(localMedia: Async<LocalMedia>) = launch {
        if (localMedia is Async.Success) {
            localMediaActions.share(localMedia.data)
                .onFailure {
                    val snackbarMessage = SnackbarMessage(mediaActionsError(it))
                    snackbarDispatcher.post(snackbarMessage)
                }
        } else Unit
    }

    private fun CoroutineScope.open(localMedia: Async<LocalMedia>) = launch {
        if (localMedia is Async.Success) {
            localMediaActions.open(localMedia.data)
                .onFailure {
                    val snackbarMessage = SnackbarMessage(mediaActionsError(it))
                    snackbarDispatcher.post(snackbarMessage)
                }
        } else Unit
    }

    private fun mediaActionsError(throwable: Throwable): Int {
        return if (throwable is ActivityNotFoundException) {
            UtilsR.string.error_no_compatible_app_found
        } else {
            CommonStrings.error_unknown
        }
    }
}



