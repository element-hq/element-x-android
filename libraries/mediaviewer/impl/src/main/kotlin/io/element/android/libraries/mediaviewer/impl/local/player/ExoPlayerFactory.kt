/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local.player

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer

@OptIn(UnstableApi::class)
@Composable
fun rememberExoPlayer(forAudioOnly: Boolean): ExoPlayer {
    return if (LocalInspectionMode.current) {
        remember {
            ExoPlayerForPreview()
        }
    } else {
        val context = LocalContext.current
        remember {
            if (forAudioOnly) {
                // Required for media3-exoplayer-midi to decode MIDI samples produced by DefaultExtractorsFactory.
                val renderersFactory = DefaultRenderersFactory(context)
                    .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                ExoPlayer.Builder(context, renderersFactory).build()
            } else {
                ExoPlayer.Builder(context).build()
            }
        }
    }
}
