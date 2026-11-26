/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.di

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.matrix.impl.proxy.ProxyProvider
import io.element.android.libraries.network.useragent.UserAgentProvider
import org.matrix.rustcomponents.sdk.HttpSettings
import org.matrix.rustcomponents.sdk.RequestConfig

@BindingContainer
@ContributesTo(AppScope::class)
object AppMatrixBindingContainer {
    @Provides
    fun provideLifecycleOwner(): LifecycleOwner = ProcessLifecycleOwner.get()

    @SingleIn(AppScope::class)
    @Provides
    fun provideHttpSettings(
        userAgentProvider: UserAgentProvider,
        proxyProvider: ProxyProvider,
    ): HttpSettings = HttpSettings(
        disableSslVerification = false,
        // Workaround for non-nullable proxy parameter in the SDK, since each call to the ClientBuilder returns a new reference we need to keep
        proxy = proxyProvider.provides(),
        userAgent = userAgentProvider.provide(),
        additionalRootCertificates = null,
        disableBuiltInRootCertificates = false,
    )

    @SingleIn(AppScope::class)
    @Provides
    fun provideRequestConfig(): RequestConfig = RequestConfig(
        timeout = 30_000uL,
        // retryLimit must be non-zero for the SDK to retry API calls in case of error (including 429 Too Many Requests error).
        retryLimit = 3u,
        // Use default values for the rest
        maxConcurrentRequests = null,
        maxRetryTime = null,
    )
}
