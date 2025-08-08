/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.uri.ensureProtocol
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.network.RetrofitFactory
import io.element.android.libraries.wellknown.api.ElementWellKnown
import io.element.android.libraries.wellknown.api.WellKnown
import io.element.android.libraries.wellknown.api.WellKnownBaseConfig
import io.element.android.libraries.wellknown.api.WellknownRetriever
import timber.log.Timber
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultWellknownRetriever @Inject constructor(
    private val retrofitFactory: RetrofitFactory,
) : WellknownRetriever {
    override suspend fun getWellKnown(baseUrl: String): WellKnown? {
        val wellknownApi = buildWellknownApi(baseUrl) ?: return null
        return try {
            wellknownApi.getWellKnown().map()
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve well-known data for $baseUrl")
            null
        }
    }

    override suspend fun getElementWellKnown(baseUrl: String): ElementWellKnown? {
        val wellknownApi = buildWellknownApi(baseUrl) ?: return null
        return try {
            wellknownApi.getElementWellKnown().map()
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve Element well-known data for $baseUrl")
            null
        }
    }

    private fun buildWellknownApi(accountProviderUrl: String): WellknownAPI? {
        return try {
            retrofitFactory.create(accountProviderUrl.ensureProtocol())
                .create(WellknownAPI::class.java)
        } catch (e: Exception) {
            // If the base URL is not valid, we cannot retrieve the well-known data
            Timber.e(e, "Failed to create Retrofit instance for $accountProviderUrl")
            null
        }
    }
}

private fun InternalElementWellKnown.map() = ElementWellKnown(
    registrationHelperUrl = registrationHelperUrl,
    enforceElementPro = enforceElementPro,
)

private fun InternalWellKnown.map() = WellKnown(
    homeServer = homeServer?.map(),
    identityServer = identityServer?.map(),
)

private fun InternalWellKnownBaseConfig.map() = WellKnownBaseConfig(
    baseURL = baseURL,
)
