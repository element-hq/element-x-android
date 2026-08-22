/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl.install

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.appconfig.PrivatePushConfig
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.runCatchingExceptions
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

interface NtfyManifestFetcher {
    /** Fail-quiet: any network/parsing problem yields null. */
    suspend fun fetch(): NtfyManifest?
}

@ContributesBinding(AppScope::class)
class DefaultNtfyManifestFetcher(
    private val okHttpClient: OkHttpClient,
    private val coroutineDispatchers: CoroutineDispatchers,
) : NtfyManifestFetcher {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun fetch(): NtfyManifest? = withContext(coroutineDispatchers.io) {
        val request = Request.Builder().url(PrivatePushConfig.NTFY_MANIFEST_URL).build()
        runCatchingExceptions {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@runCatchingExceptions null
                json.decodeFromString(NtfyManifest.serializer(), response.body.string())
            }
        }.getOrNull()
    }
}
