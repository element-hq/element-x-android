/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.network

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.network.interceptors.DynamicHttpLoggingInterceptor
import io.element.android.libraries.network.interceptors.FormattedJsonHttpLogger
import io.element.android.libraries.network.interceptors.UserAgentInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

@BindingContainer
@ContributesTo(AppScope::class)
object NetworkModule {
    @Provides
    @SingleIn(AppScope::class)
    fun providesOkHttpClient(
        userAgentInterceptor: UserAgentInterceptor,
        dynamicHttpLoggingInterceptor: DynamicHttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder().apply {
        connectTimeout(30, TimeUnit.SECONDS)
        readTimeout(60, TimeUnit.SECONDS)
        writeTimeout(60, TimeUnit.SECONDS)
        addInterceptor(userAgentInterceptor)
        addInterceptor(dynamicHttpLoggingInterceptor)
    }.build()

    @Provides
    @SingleIn(AppScope::class)
    fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val logger = FormattedJsonHttpLogger(HttpLoggingInterceptor.Level.BODY)
        return HttpLoggingInterceptor(logger)
    }
}
