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

package io.element.android.libraries.matrix.ui.media

import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.MatrixClient
import javax.inject.Inject

class LoggedInImageLoaderFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val matrixClient: MatrixClient,
) : ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader
            .Builder(context)
            .components {
                add(MediaKeyer())
                add(MediaFetcher.Factory(matrixClient))
            }
            .build()
    }
}

class NotLoggedInImageLoaderFactory @Inject constructor(
    @ApplicationContext private val context: Context,
) : ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader
            .Builder(context)
            .build()
    }
}
