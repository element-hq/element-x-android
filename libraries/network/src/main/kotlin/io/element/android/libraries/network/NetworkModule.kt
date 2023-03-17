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

package io.element.android.libraries.network

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import okhttp3.OkHttpClient
import okhttp3.Protocol
import io.element.android.libraries.network.interceptors.FormattedJsonHttpLogger
import java.util.concurrent.TimeUnit
import okhttp3.logging.HttpLoggingInterceptor

@Module
@ContributesTo(AppScope::class)
object NetworkModule {

    @Provides
    @JvmStatic
    fun providesHttpLoggingInterceptor(buildMeta: BuildMeta): HttpLoggingInterceptor {
        val logger = FormattedJsonHttpLogger(buildMeta.okHttpLoggingLevel)
        val interceptor = HttpLoggingInterceptor(logger)
        interceptor.level = buildMeta.okHttpLoggingLevel
        return interceptor
    }

    @Provides
    @SingleIn(AppScope::class)
    fun providesOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            // workaround for #4669
            .protocols(listOf(Protocol.HTTP_1_1))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }
}
