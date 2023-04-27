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

package io.element.android.libraries.designsystem.components.blurhash

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage

@Composable
fun BlurHashAsyncImage(
    model: Any?,
    blurHash: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    contentDescription: String? = null,
) {
    SubcomposeAsyncImage(
        model = model,
        modifier = modifier,
        contentScale = contentScale,
        contentDescription = contentDescription,
        loading = {
            BlurHashImage(
                blurHash = blurHash,
                contentScale = contentScale,
                contentDescription = "Loading placeholder"
            )
        },
    )
}

@Composable
fun BlurHashImage(
    blurHash: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val bitmapState = remember {
        mutableStateOf<Bitmap?>(null)
    }
    DisposableEffect(blurHash) {
        // Build a small blurhash image so that it's fast
        bitmapState.value = BlurHashDecoder.decode(blurHash, 10, 10)
        onDispose {
            bitmapState.value?.recycle()
        }
    }
    bitmapState.value?.let { bitmap ->
        Image(
            modifier = modifier.fillMaxSize(),
            bitmap = bitmap.asImageBitmap(),
            contentScale = contentScale,
            contentDescription = contentDescription
        )
    }
}
