/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.network

import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.GetUrlResolver
import io.element.android.libraries.matrix.api.exception.ClientException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException

class AndroidGetUrlResolver(
    private val okHttpClient: OkHttpClient,
) : GetUrlResolver {
    override suspend fun getUrl(url: String): Result<ByteArray> = runCatchingExceptions {
        suspendCancellableCoroutine { continuation ->
            val request = Request.Builder().url(url).build()
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWith(Result.failure(e))
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val body = response.body.bytes()
                        continuation.resumeWith(Result.success(body))
                    } else {
                        continuation.resumeWith(Result.failure(ClientException.Generic("HTTP error code: ${response.code}", null)))
                    }
                }
            })
        }
    }
}
