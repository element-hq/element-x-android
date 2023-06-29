/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
