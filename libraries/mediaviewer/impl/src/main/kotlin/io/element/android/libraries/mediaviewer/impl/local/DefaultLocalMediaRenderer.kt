/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.viewfolder.api.TextFileViewer
import io.element.android.libraries.audio.api.AudioFocus
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.api.local.LocalMediaRenderer
import me.saket.telephoto.zoomable.OverzoomEffect
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.rememberZoomableState
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultLocalMediaRenderer @Inject constructor(
    private val textFileViewer: TextFileViewer,
    private val audioFocus: AudioFocus,
) : LocalMediaRenderer {
    @Composable
    override fun Render(localMedia: LocalMedia) {
        val localMediaViewState = rememberLocalMediaViewState(
            zoomableState = rememberZoomableState(
                zoomSpec = ZoomSpec(maxZoomFactor = 4f, overzoomEffect = OverzoomEffect.NoLimits)
            )
        )
        LocalMediaView(
            modifier = Modifier.fillMaxSize(),
            bottomPaddingInPixels = 0,
            localMedia = localMedia,
            localMediaViewState = localMediaViewState,
            textFileViewer = textFileViewer,
            audioFocus = audioFocus,
            onClick = {},
        )
    }
}
