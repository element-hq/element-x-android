/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.network.interceptors

import dev.zacsweers.metro.Inject
import io.element.android.libraries.network.headers.HttpHeaders
import io.element.android.libraries.network.useragent.UserAgentProvider
import okhttp3.Interceptor
import okhttp3.Response

@Inject
class UserAgentInterceptor(
    private val userAgentProvider: UserAgentProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest = chain.request()
            .newBuilder()
            .header(HttpHeaders.UserAgent, userAgentProvider.provide())
            .build()
        return chain.proceed(newRequest)
    }
}
