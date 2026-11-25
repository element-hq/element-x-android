/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.di

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.impl.proxy.ProxyProvider
import io.element.android.libraries.network.useragent.SimpleUserAgentProvider
import org.junit.Test

class AppMatrixBindingContainerTest {
    @Test
    fun `provideHttpSettings uses the user agent and the proxy`() {
        val httpSettings = AppMatrixBindingContainer.provideHttpSettings(
            userAgentProvider = SimpleUserAgentProvider("A user agent"),
            proxyProvider = aProxyProvider("http://proxy.org:8080"),
        )
        assertThat(httpSettings.userAgent).isEqualTo("A user agent")
        assertThat(httpSettings.proxy).isEqualTo("http://proxy.org:8080")
        assertThat(httpSettings.disableSslVerification).isFalse()
        assertThat(httpSettings.additionalRootCertificates).isNull()
        assertThat(httpSettings.disableBuiltInRootCertificates).isFalse()
    }

    @Test
    fun `provideHttpSettings has no proxy when none is configured`() {
        val httpSettings = AppMatrixBindingContainer.provideHttpSettings(
            userAgentProvider = SimpleUserAgentProvider(),
            proxyProvider = aProxyProvider(null),
        )
        assertThat(httpSettings.proxy).isNull()
    }

    @Test
    fun `provideRequestConfig sets a timeout and allows retries`() {
        val requestConfig = AppMatrixBindingContainer.provideRequestConfig()
        assertThat(requestConfig.timeout).isEqualTo(30_000uL)
        assertThat(requestConfig.retryLimit).isEqualTo(3uL)
        assertThat(requestConfig.maxConcurrentRequests).isNull()
        assertThat(requestConfig.maxRetryTime).isNull()
    }
}

private fun aProxyProvider(proxy: String?) = object : ProxyProvider {
    override fun provides(): String? = proxy
}
