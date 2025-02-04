/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */
package io.element.android.features.login.impl.resolver.network

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.network.RetrofitFactory
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultWellknownRequest @Inject constructor(
    private val retrofitFactory: RetrofitFactory,
) : WellknownRequest {
    /**
     * Return the WellKnown data, if found.
     * @param baseUrl for instance https://matrix.org
     */
    override suspend fun execute(baseUrl: String): WellKnown {
        val wellknownApi = retrofitFactory.create(baseUrl)
            .create(WellknownAPI::class.java)
        return wellknownApi.getWellKnown()
    }
}
