/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local

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
        val isShowingControls: Boolean,
    ) : PlayableState
}

@Composable
fun rememberLocalMediaViewState(zoomableState: ZoomableState = rememberZoomableState()): LocalMediaViewState {
    return remember(zoomableState) {
        LocalMediaViewState(zoomableState)
    }
}
