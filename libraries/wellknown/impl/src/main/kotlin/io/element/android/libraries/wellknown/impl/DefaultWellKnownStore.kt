/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.androidutils.json.JsonProvider
import io.element.android.libraries.cachestore.api.CacheData
import io.element.android.libraries.cachestore.api.CacheStore
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.wellknown.api.ElementWellKnown
import io.element.android.libraries.wellknown.api.ElementWellknownStore
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult
import io.element.android.services.toolbox.api.systemclock.SystemClock

@ContributesBinding(AppScope::class)
class DefaultWellKnownStore(
    private val cacheStore: CacheStore,
    private val json: JsonProvider,
    private val systemClock: SystemClock,
) : ElementWellknownStore {
    override suspend fun get(domain: String): WellknownRetrieverResult<ElementWellKnown> {
        return runCatchingExceptions {
            val cachedData = cacheStore.getData(key(domain))
            if (cachedData != null) {
                val data = json().decodeFromString<InternalElementWellKnown>(cachedData.value).map()
                if (systemClock.epochMillis() > cachedData.updatedAt + CACHE_VALIDITY_MILLIS) {
                    WellknownRetrieverResult.Outdated(data)
                } else {
                    WellknownRetrieverResult.Success(data)
                }
            } else {
                WellknownRetrieverResult.NotFound
            }
        }.recover {
            WellknownRetrieverResult.Error(it as Exception)
        }.getOrThrow()
    }

    override suspend fun update(domain: String, wellknown: String): Result<Unit> {
        return runCatchingExceptions {
            cacheStore.storeData(key(domain), CacheData(wellknown, systemClock.epochMillis()))
        }
    }

    override suspend fun delete(domain: String): Result<Unit> {
        return runCatchingExceptions {
            cacheStore.deleteData(key(domain))
        }
    }

    private fun key(domain: String): String = "https://$domain/.well-known/element/element.json"

    companion object {
        // 1 day
        private const val CACHE_VALIDITY_MILLIS = 1 * 24 * 60 * 60 * 1000L
    }
}
