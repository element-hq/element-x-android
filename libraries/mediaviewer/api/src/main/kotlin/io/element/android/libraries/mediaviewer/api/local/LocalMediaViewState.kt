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

package io.element.android.libraries.mediaviewer.api.local

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import me.saket.telephoto.zoomable.ZoomableState
import me.saket.telephoto.zoomable.rememberZoomableState

@Stable
class LocalMediaViewState internal constructor(
    val zoomableState: ZoomableState,
) {
    var isReady: Boolean by mutableStateOf(false)
    var playableState: PlayableState by mutableStateOf(PlayableState.NotPlayable)
}

@Immutable
sealed interface PlayableState {
    data object NotPlayable : PlayableState
    data class Playable(
        val isPlaying: Boolean,
        val isShowingControls: Boolean
    ) : PlayableState
}

@Composable
fun rememberLocalMediaViewState(zoomableState: ZoomableState = rememberZoomableState()): LocalMediaViewState {
    return remember(zoomableState) {
        LocalMediaViewState(zoomableState)
    }
}
