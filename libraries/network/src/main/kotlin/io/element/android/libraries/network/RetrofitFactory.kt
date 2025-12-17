/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.network

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import io.element.android.libraries.androidutils.json.JsonProvider
import io.element.android.libraries.core.uri.ensureTrailingSlash
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Inject
class RetrofitFactory(
    private val okHttpClient: Provider<OkHttpClient>,
    private val json: Provider<JsonProvider>,
) {
    fun create(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl.ensureTrailingSlash())
        .addConverterFactory(json()().asConverterFactory("application/json".toMediaType()))
        .callFactory { request -> okHttpClient().newCall(request) }
        .build()
}
