/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.impl

import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.libraries.core.extensions.mapCatchingExceptions
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.exception.ClientException
import io.element.android.libraries.wellknown.api.ElementWellKnown
import io.element.android.libraries.wellknown.api.ElementWellKnownParser
import io.element.android.libraries.wellknown.api.ElementWellKnownSource
import io.element.android.libraries.wellknown.api.ElementWellknownStore
import io.element.android.libraries.wellknown.api.SessionWellknownRetriever
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@ContributesBinding(SessionScope::class)
class DefaultSessionWellknownRetriever(
    private val matrixClient: MatrixClient,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
    private val elementWellknownStoreFactory: ElementWellknownStore.Factory,
    private val elementWellKnownParser: ElementWellKnownParser,
    private val enterpriseService: EnterpriseService,
) : SessionWellknownRetriever {
    private val domain by lazy { matrixClient.userIdServerName() }

    override suspend fun getElementWellKnown(source: ElementWellKnownSource): WellknownRetrieverResult<ElementWellKnown> {
        val overriddenElementWellKnown = enterpriseService.overriddenElementWellKnown()
        if (overriddenElementWellKnown != null) {
            return WellknownRetrieverResult.Success(overriddenElementWellKnown)
        }

        // We instantiate different stores for different sources, so that we can cache them separately.
        // The ESS_CONFIG source is cached with a prefix to differentiate it from the WELLKNOWN_ENDPOINT.
        val prefix = when (source) {
            ElementWellKnownSource.ESS_CONFIG -> "ess_config"
            ElementWellKnownSource.WELLKNOWN_ENDPOINT -> null
        }
        val store = elementWellknownStoreFactory.create(prefix = prefix)

        return when (val cacheData = store.get(domain)) {
            is WellknownRetrieverResult.Success -> {
                Timber.d("Using cached well-known for domain $domain")
                cacheData
            }
            is WellknownRetrieverResult.Outdated -> {
                // Return the outdated data but refresh in the background
                // If the cache is missing or outdated, trigger a refresh in background but still return the cached value
                Timber.d("Outdated cached well-known for domain $domain, returning existing value and fetching new one from network")
                sessionCoroutineScope.launch { fetchElementWellKnown(url(source), store) }
                cacheData
            }
            is WellknownRetrieverResult.NotFound -> {
                // Try to fetch from the server
                Timber.d("No cached well-known for domain $domain, fetching from network")
                fetchElementWellKnown(url(source), store)
            }
            is WellknownRetrieverResult.Error -> {
                // Return the error
                Timber.e(cacheData.exception, "Error retrieving well-known for domain $domain")
                cacheData.exception.toWellknownRetrieverResult()
            }
        }
    }

    private suspend fun fetchElementWellKnown(url: String, store: ElementWellknownStore): WellknownRetrieverResult<ElementWellKnown> {
        return matrixClient
            .getUrl(url)
            .mapCatchingExceptions {
                val data = String(it)
                val parsed = elementWellKnownParser.parse(data).getOrThrow()
                // Also store in cache, if valid
                store.update(domain, data)
                    .onFailure { exception ->
                        Timber.e(exception, "Failed to parse cached Element .well-known data for $domain, deleting cache")
                        store.delete(domain)
                    }
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

    private fun url(source: ElementWellKnownSource): String {
        val elementWellKnownUrl = "https://$domain/.well-known/element/element.json"
        return when (source) {
            ElementWellKnownSource.ESS_CONFIG -> enterpriseService.essConfigEndpointUrl(domain) ?: elementWellKnownUrl
            ElementWellKnownSource.WELLKNOWN_ENDPOINT -> elementWellKnownUrl
        }
    }
}
