/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.network

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.network.interceptors.FormattedJsonHttpLogger
import io.element.android.libraries.network.interceptors.UserAgentInterceptor
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

@Module
@ContributesTo(AppScope::class)
object NetworkModule {
    @Provides
    @SingleIn(AppScope::class)
    fun providesOkHttpClient(
        buildMeta: BuildMeta,
        userAgentInterceptor: UserAgentInterceptor,
    ): OkHttpClient = OkHttpClient.Builder().apply {
        connectTimeout(30, TimeUnit.SECONDS)
        readTimeout(60, TimeUnit.SECONDS)
        writeTimeout(60, TimeUnit.SECONDS)
        addInterceptor(userAgentInterceptor)
        if (buildMeta.isDebuggable) addInterceptor(providesHttpLoggingInterceptor())
    }.build()

    @Provides
    @SingleIn(AppScope::class)
    fun providesJson(): Json = Json {
        ignoreUnknownKeys = true
    }
}

private fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor {
    val loggingLevel = HttpLoggingInterceptor.Level.BODY
    val logger = FormattedJsonHttpLogger(loggingLevel)
    val interceptor = HttpLoggingInterceptor(logger)
    interceptor.level = loggingLevel
    return interceptor
}
