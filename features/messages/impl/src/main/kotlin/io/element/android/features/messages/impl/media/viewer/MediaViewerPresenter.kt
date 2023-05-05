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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.messages.impl.media.local.LocalMedia
import io.element.android.features.messages.impl.media.local.LocalMediaFactory
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.media.MatrixMediaSource

class MediaViewerPresenter @AssistedInject constructor(
    @Assisted private val name: String,
    @Assisted private val mediaSource: MatrixMediaSource,
    private val localMediaFactory: LocalMediaFactory,
    private val client: MatrixClient,
) : Presenter<MediaViewerState> {

    @AssistedFactory
    interface Factory {
        fun create(name: String, mediaSource: MatrixMediaSource): MediaViewerPresenter
    }

    @Composable
    override fun present(): MediaViewerState {
        val localMedia by produceState<Async<LocalMedia>>(initialValue = Async.Uninitialized) {
            value = Async.Loading(null)
            //TODO we are missing some permissions to use this API
            client.mediaLoader.loadMediaFile(mediaSource, null)
                .onSuccess {
                    val localMedia = localMediaFactory.createFromUri(uri = it, null)
                    Async.Success(localMedia)
                }.onFailure {
                    Async.Failure(it, null)
                }
        }

        return MediaViewerState(
            name = name,
            downloadedMedia = localMedia,
        )
    }
}
