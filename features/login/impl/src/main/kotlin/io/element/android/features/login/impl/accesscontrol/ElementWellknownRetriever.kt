/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.accesscontrol

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.login.impl.resolver.network.ElementWellKnown
import io.element.android.features.login.impl.resolver.network.WellknownAPI
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.network.RetrofitFactory
import timber.log.Timber
import javax.inject.Inject

interface ElementWellknownRetriever {
    suspend fun retrieve(accountProviderUrl: String): ElementWellKnown?
}

@ContributesBinding(AppScope::class)
class DefaultElementWellknownRetriever @Inject constructor(
    private val retrofitFactory: RetrofitFactory,
) : ElementWellknownRetriever {
    override suspend fun retrieve(accountProviderUrl: String): ElementWellKnown? {
        val wellknownApi = try {
            retrofitFactory.create(accountProviderUrl)
                .create(WellknownAPI::class.java)
        } catch (e: Exception) {
            // If the base URL is not valid, we cannot retrieve the well-known data
            Timber.e(e, "Failed to create Retrofit instance for $accountProviderUrl")
            return null
        }
        return try {
            wellknownApi.getElementWellKnown()
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve Element well-known data for $accountProviderUrl")
            null
        }
    }
}
