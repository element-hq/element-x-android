/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.impl

import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.androidutils.json.JsonProvider
import io.element.android.libraries.cachestore.api.CacheData
import io.element.android.libraries.cachestore.api.CacheStore
import io.element.android.libraries.core.extensions.mapCatchingExceptions
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.exception.ClientException
import io.element.android.libraries.wellknown.api.ElementWellKnown
import io.element.android.libraries.wellknown.api.SessionWellknownRetriever
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@ContributesBinding(SessionScope::class)
class DefaultSessionWellknownRetriever(
    private val matrixClient: MatrixClient,
    private val json: JsonProvider,
    private val cacheStore: CacheStore,
    private val systemClock: SystemClock,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
) : SessionWellknownRetriever {
    private val domain by lazy { matrixClient.userIdServerName() }

    override suspend fun getElementWellKnown(): WellknownRetrieverResult<ElementWellKnown> {
        val url = "https://$domain/.well-known/element/element.json"
        val cacheData = cacheStore.getData(url)
        if (cacheData != null) {
            Timber.d("Element .well-known data retrieved from cache for $domain")
            // If the cache is outdated, trigger a refresh in background but still return the cached value
            if (systemClock.epochMillis() > cacheData.updatedAt + CACHE_VALIDITY_MILLIS) {
                sessionCoroutineScope.launch {
                    fetchElementWellKnown(url)
                }
            }
            try {
                val parsed = json().decodeFromString<InternalElementWellKnown>(cacheData.value).map()
                return WellknownRetrieverResult.Success(parsed)
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse cached Element .well-known data for $domain, deleting cache")
                cacheStore.deleteData(url)
            }
        }

        return fetchElementWellKnown(url)
    }

    private suspend fun fetchElementWellKnown(url: String): WellknownRetrieverResult<ElementWellKnown> {
        return matrixClient
            .getUrl(url)
            .mapCatchingExceptions {
                val data = String(it)
                val parsed = json().decodeFromString<InternalElementWellKnown>(data).map()
                // Also store in cache, if valid
                cacheStore.storeData(
                    key = url,
                    data = CacheData(
                        value = data,
                        updatedAt = systemClock.epochMillis(),
                    )
                )
                parsed
            }
            .toWellknownRetrieverResult()
    }

    private fun <T> Result<T>.toWellknownRetrieverResult(): WellknownRetrieverResult<T> = fold(
        onSuccess = {
            WellknownRetrieverResult.Success(it)
        },
        onFailure = {
            Timber.e(it, "Failed to retrieve Element .well-known from $domain")
            // This check on message value is not ideal but this is what we got from the SDK.
            if ((it as? ClientException.Generic)?.message?.contains("404") == true) {
                WellknownRetrieverResult.NotFound
            } else {
                WellknownRetrieverResult.Error(it as Exception)
            }
        }
    )

    companion object {
        // 1 day
        private const val CACHE_VALIDITY_MILLIS = 1 * 24 * 60 * 60 * 1000L
    }
}
