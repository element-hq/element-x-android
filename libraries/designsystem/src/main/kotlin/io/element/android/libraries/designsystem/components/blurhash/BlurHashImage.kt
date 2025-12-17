/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.blurhash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import com.vanniktech.blurhash.BlurHash

@Suppress("ModifierMissing")
@Composable
fun BlurHashImage(
    blurHash: String?,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
) {
    if (blurHash == null) return
    val blurHashImage = rememberBlurHashImage(blurHash)
    blurHashImage?.let { bitmap ->
        Image(
            modifier = Modifier.fillMaxSize(),
            bitmap = bitmap,
            contentScale = contentScale,
            contentDescription = contentDescription
        )
    }
}

@Composable
fun rememberBlurHashImage(blurHash: String?): ImageBitmap? {
    return if (LocalInspectionMode.current) {
        blurHash?.let { BlurHash.decode(it, 10, 10)?.asImageBitmap() }
    } else {
        produceState<ImageBitmap?>(initialValue = null, blurHash) {
            blurHash?.let { value = BlurHash.decode(it, 10, 10)?.asImageBitmap() }
        }.value
    }
}
