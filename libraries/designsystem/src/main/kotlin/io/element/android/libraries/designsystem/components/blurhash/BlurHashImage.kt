/*
 * Copyright (c) 2024 New Vector Ltd
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
