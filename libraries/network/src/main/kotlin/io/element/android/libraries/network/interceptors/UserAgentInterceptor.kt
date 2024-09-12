/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.network.interceptors

import io.element.android.libraries.network.headers.HttpHeaders
import io.element.android.libraries.network.useragent.UserAgentProvider
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class UserAgentInterceptor @Inject constructor(
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
