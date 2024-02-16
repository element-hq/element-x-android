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

package io.element.android.libraries.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.vanniktech.blurhash.BlurHash

@Composable
fun BlurHashAsyncImage(
    model: Any?,
    blurHash: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    contentDescription: String? = null,
) {
    var isLoading by rememberSaveable(model) { mutableStateOf(true) }
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = model,
            contentScale = contentScale,
            contentDescription = contentDescription,
            onSuccess = { isLoading = false }
        )
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            BlurHashImage(
                blurHash = blurHash,
                contentDescription = contentDescription,
                contentScale = ContentScale.FillBounds,
            )
        }
    }
}

@Composable
private fun BlurHashImage(
    blurHash: String?,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
) {
    if (blurHash == null) return
    val bitmapState = remember(blurHash) {
        mutableStateOf(
            // Build a small blurhash image so that it's fast
            BlurHash.decode(blurHash, 10, 10)
        )
    }
    DisposableEffect(blurHash) {
        onDispose {
            bitmapState.value?.recycle()
        }
    }
    bitmapState.value?.let { bitmap ->
        Image(
            modifier = Modifier.fillMaxSize(),
            bitmap = bitmap.asImageBitmap(),
            contentScale = contentScale,
            contentDescription = contentDescription
        )
    }
}
