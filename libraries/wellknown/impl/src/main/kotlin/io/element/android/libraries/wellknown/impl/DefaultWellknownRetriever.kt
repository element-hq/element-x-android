/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.libraries.androidutils.text.takeIfNotBlank
import io.element.android.libraries.core.extensions.mapCatchingExceptions
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.uri.ensureProtocol
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.matrix.api.UrlContentFetcher
import io.element.android.libraries.matrix.api.exception.ClientException
import io.element.android.libraries.wellknown.api.ElementWellKnown
import io.element.android.libraries.wellknown.api.ElementWellKnownParser
import io.element.android.libraries.wellknown.api.ElementWellknownStore
import io.element.android.libraries.wellknown.api.EnterpriseRemoteConfigSource
import io.element.android.libraries.wellknown.api.WellknownRetriever
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.URL

@AssistedInject
class DefaultWellknownRetriever(
    private val elementWellknownStoreFactory: ElementWellknownStore.Factory,
    private val enterpriseService: EnterpriseService,
    private val elementWellKnownParser: ElementWellKnownParser,
    @Assisted private val urlContentFetcher: UrlContentFetcher,
    @AppCoroutineScope private val coroutineScope: CoroutineScope,
) : WellknownRetriever {
    @ContributesBinding(AppScope::class)
    @AssistedFactory
    fun interface Factory : WellknownRetriever.Factory {
        override fun create(urlContentFetcher: UrlContentFetcher): DefaultWellknownRetriever
    }

    override suspend fun getElementWellKnown(
        host: String,
        source: EnterpriseRemoteConfigSource
    ): WellknownRetrieverResult<ElementWellKnown> {
        val overriddenElementWellKnown = enterpriseService.overriddenElementWellKnown()
        if (overriddenElementWellKnown != null) {
            return WellknownRetrieverResult.Success(overriddenElementWellKnown)
        }

        // The passed `host` function may be a full URL, in that case we're only interested in the actual host
        val checkedHost = runCatchingExceptions { URL(host.ensureProtocol()).host.takeIfNotBlank() }
            .getOrElse { return WellknownRetrieverResult.Error(IllegalArgumentException("host parameter is not valid", it)) }
            ?: host

        // We instantiate different stores for different sources, so that we can cache them separately.
        // The ESS_CONFIG source is cached with a prefix to differentiate it from the WELLKNOWN_ENDPOINT.
        val prefix = when (source) {
            EnterpriseRemoteConfigSource.ESS_CONFIG -> "ess_config"
            EnterpriseRemoteConfigSource.WELLKNOWN_ENDPOINT -> null
        }
        val store = elementWellknownStoreFactory.create(prefix)
        return when (val cacheData = store.get(checkedHost)) {
            is WellknownRetrieverResult.Success -> {
                Timber.d("Using cached well-known for domain $checkedHost")
                cacheData
            }
            is WellknownRetrieverResult.Outdated -> {
                // Return the outdated data but refresh in the background
                // If the cache is missing or outdated, trigger a refresh in background but still return the cached value
                Timber.d("Outdated cached well-known for domain $checkedHost, returning existing value and fetching new one from network")
                coroutineScope.launch { fetchElementWellKnown(checkedHost, source, store) }
                cacheData
            }
            is WellknownRetrieverResult.NotFound -> {
                // Try to fetch from the server
                Timber.d("No cached well-known for domain $checkedHost, fetching from network")
                fetchElementWellKnown(checkedHost, source, store)
            }
            is WellknownRetrieverResult.Error -> {
                // Return the error
                Timber.e(cacheData.exception, "Error retrieving well-known for domain $checkedHost")
                cacheData.exception.toWellknownRetrieverResult()
            }
        }
    }

    private suspend fun fetchElementWellKnown(
        host: String,
        source: EnterpriseRemoteConfigSource,
        store: ElementWellknownStore
    ): WellknownRetrieverResult<ElementWellKnown> {
        return urlContentFetcher
            .getUrl(url(host, source))
            .mapCatchingExceptions {
                val data = String(it)
                val parsed = elementWellKnownParser.parse(data).getOrThrow()
                // Also store in cache, if valid
                store.update(host, data)
                    .onFailure { exception ->
                        Timber.e(exception, "Failed to parse cached Element .well-known data for $host, deleting cache")
                        store.delete(host)
                    }
                parsed
            }
            .toWellknownRetrieverResult(host)
    }

    private fun <T> Result<T>.toWellknownRetrieverResult(host: String): WellknownRetrieverResult<T> = fold(
        onSuccess = {
            WellknownRetrieverResult.Success(it)
        },
        onFailure = {
            Timber.e(it, "Failed to retrieve Element .well-known from $host")
            // This check on message value is not ideal but this is what we got from the SDK.
            it.toWellknownRetrieverResult()
        }
    )

    private fun <T> Throwable.toWellknownRetrieverResult(): WellknownRetrieverResult<T> {
        return if ((this as? ClientException.Generic)?.message?.contains("404") == true) {
            WellknownRetrieverResult.NotFound
        } else {
            WellknownRetrieverResult.Error(this as Exception)
        }
    }

    private fun url(host: String, source: EnterpriseRemoteConfigSource): String {
        val elementWellKnownUrl = "https://$host/.well-known/element/element.json"
        return when (source) {
            EnterpriseRemoteConfigSource.ESS_CONFIG -> enterpriseService.essConfigEndpointUrl(host) ?: elementWellKnownUrl
            EnterpriseRemoteConfigSource.WELLKNOWN_ENDPOINT -> elementWellKnownUrl
        }
    }
}
