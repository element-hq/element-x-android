/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media.test

import coil3.ComponentRegistry
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.Disposable
import coil3.request.ImageRequest
import coil3.request.ImageResult

class FakeImageLoader : ImageLoader {
    private val executedRequests = mutableListOf<ImageRequest>()

    override val defaults: ImageRequest.Defaults
        get() = error("Not implemented")
    override val components: ComponentRegistry
        get() = error("Not implemented")
    override val memoryCache: MemoryCache?
        get() = error("Not implemented")
    override val diskCache: DiskCache?
        get() = error("Not implemented")

    override fun enqueue(request: ImageRequest): Disposable {
        error("Not implemented")
    }

    override suspend fun execute(request: ImageRequest): ImageResult {
        executedRequests.add(request)
        error("Not implemented")
    }

    override fun shutdown() {
        error("Not implemented")
    }

    override fun newBuilder(): ImageLoader.Builder {
        error("Not implemented")
    }

    fun getExecutedRequestsData(): List<Any> {
        return executedRequests.map { it.data }
    }
}
