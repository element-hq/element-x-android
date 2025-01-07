/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.network

import io.element.android.libraries.core.uri.ensureTrailingSlash
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Inject
import javax.inject.Provider

class RetrofitFactory @Inject constructor(
    private val okHttpClient: Provider<OkHttpClient>,
    private val json: Provider<Json>,
) {
    fun create(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl.ensureTrailingSlash())
        .addConverterFactory(json.get().asConverterFactory("application/json".toMediaType()))
        .callFactory { request -> okHttpClient.get().newCall(request) }
        .build()
}
