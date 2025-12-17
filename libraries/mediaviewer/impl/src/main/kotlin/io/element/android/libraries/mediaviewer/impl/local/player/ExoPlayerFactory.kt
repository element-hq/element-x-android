/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.media3.exoplayer.ExoPlayer

@Composable
fun rememberExoPlayer(): ExoPlayer {
    return if (LocalInspectionMode.current) {
        remember {
            ExoPlayerForPreview()
        }
    } else {
        val context = LocalContext.current
        remember {
            ExoPlayer.Builder(context).build()
        }
    }
}
