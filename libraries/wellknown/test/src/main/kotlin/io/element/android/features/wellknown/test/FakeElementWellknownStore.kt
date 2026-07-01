/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.wellknown.test

import io.element.android.libraries.wellknown.api.ElementWellKnown
import io.element.android.libraries.wellknown.api.ElementWellknownStore
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult
import java.net.URL

class FakeElementWellknownStore(
    initialData: Map<String, WellknownRetrieverResult<ElementWellKnown>> = emptyMap(),
) : ElementWellknownStore {
    private val cachedData = mutableMapOf<String, WellknownRetrieverResult<ElementWellKnown>>()

    init {
        initialData.forEach { (url, result) ->
            cachedData[URL(url).host] = result
        }
    }

    override suspend fun get(domain: String): WellknownRetrieverResult<ElementWellKnown> {
        return cachedData[domain] ?: WellknownRetrieverResult.NotFound
    }

    override suspend fun update(domain: String, wellKnown: ElementWellKnown): Result<Unit> {
        cachedData[domain] = WellknownRetrieverResult.Success(wellKnown)
        return Result.success(Unit)
    }

    override suspend fun delete(domain: String): Result<Unit> {
        cachedData.remove(domain)
        return Result.success(Unit)
    }
}
