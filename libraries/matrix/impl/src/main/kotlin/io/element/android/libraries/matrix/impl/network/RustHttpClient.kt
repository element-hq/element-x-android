/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.network

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.extensions.mapFailure
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.network.HttpClient
import org.matrix.rustcomponents.sdk.HttpSettings
import org.matrix.rustcomponents.sdk.RequestConfig

@AssistedInject
class RustHttpClient(
    private val httpSettings: HttpSettings,
    private val requestConfig: RequestConfig,
    @Assisted override val homeserverUrl: String,
    @Assisted override val serverName: String?,
) : HttpClient {
    @AssistedFactory
    @ContributesBinding(AppScope::class)
    interface Factory : HttpClient.Factory {
        override fun create(homeserverUrl: String, serverName: String?): RustHttpClient
    }
    override suspend fun getUrl(url: String): Result<ByteArray> = runCatchingExceptions {
        val client = org.matrix.rustcomponents.sdk.HttpClient.fromSettings(httpSettings, requestConfig)
        client.getUrl(url)
    }.mapFailure { it.mapHttpClientException() }
}
