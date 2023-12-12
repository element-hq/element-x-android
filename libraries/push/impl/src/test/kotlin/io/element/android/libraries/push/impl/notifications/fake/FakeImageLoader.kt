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

package io.element.android.libraries.push.impl.notifications.fake

import coil.ComponentRegistry
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.DefaultRequestOptions
import coil.request.Disposable
import coil.request.ImageRequest
import coil.request.ImageResult

class FakeImageLoader : ImageLoader {
    override val components: ComponentRegistry = error("Fake class")
    override val defaults: DefaultRequestOptions = error("Fake class")
    override val diskCache: DiskCache? = null
    override val memoryCache: MemoryCache? = null

    override fun enqueue(request: ImageRequest): Disposable {
        error("Fake class")
    }

    override suspend fun execute(request: ImageRequest): ImageResult {
        error("Fake class")
    }

    override fun newBuilder(): ImageLoader.Builder {
        error("Fake class")
    }

    override fun shutdown() = Unit
}
