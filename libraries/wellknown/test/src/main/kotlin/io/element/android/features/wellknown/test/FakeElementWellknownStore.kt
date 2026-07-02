/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.wellknown.test

import io.element.android.libraries.androidutils.json.DefaultJsonProvider
import io.element.android.libraries.androidutils.json.JsonProvider
import io.element.android.libraries.wellknown.api.ElementWellKnown
import io.element.android.libraries.wellknown.api.ElementWellknownStore
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult
import io.element.android.libraries.wellknown.impl.InternalElementWellKnown
import io.element.android.libraries.wellknown.impl.map
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonDecodingException
import java.net.URL

class FakeElementWellknownStore(
    initialData: Map<String, WellknownRetrieverResult<ElementWellKnown>> = emptyMap(),
    private val jsonProvider: JsonProvider = DefaultJsonProvider(),
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

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun update(domain: String, wellknown: String): Result<Unit> {
        val json = jsonProvider()
        val mapped = try {
            json.decodeFromString<InternalElementWellKnown>(wellknown).map()
        } catch (e: JsonDecodingException) {
            return Result.failure(e)
        }
        cachedData[domain] = WellknownRetrieverResult.Success(mapped)
        return Result.success(Unit)
    }

    override suspend fun delete(domain: String): Result<Unit> {
        cachedData.remove(domain)
        return Result.success(Unit)
    }
}
