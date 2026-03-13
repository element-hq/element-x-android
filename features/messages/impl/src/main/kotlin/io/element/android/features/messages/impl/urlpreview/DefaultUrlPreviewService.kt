/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.urlpreview

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.androidutils.json.JsonProvider
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.exception.ClientException
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@ContributesBinding(SessionScope::class)
@SingleIn(SessionScope::class)
class DefaultUrlPreviewService @Inject constructor(
    private val matrixClient: MatrixClient,
    private val sessionStore: SessionStore,
    private val json: JsonProvider,
) : UrlPreviewService {
    private val cache = mutableMapOf<String, UrlPreviewData?>()
    private val cacheMutex = Mutex()

    override suspend fun getPreview(url: String): Result<UrlPreviewData?> {
        cacheMutex.withLock {
            if (cache.containsKey(url)) {
                return Result.success(cache[url])
            }
        }

        val result = fetchPreview(url)
        result.getOrNull()?.let { preview ->
            cacheMutex.withLock {
                cache[url] = preview
            }
        }
        if (result.isSuccess && result.getOrNull() == null) {
            cacheMutex.withLock {
                cache[url] = null
            }
        }
        return result
    }

    private suspend fun fetchPreview(url: String): Result<UrlPreviewData?> {
        val sessionData = sessionStore.getSession(matrixClient.sessionId.value)
            ?: return Result.failure(IllegalStateException("Missing session"))
        val homeserverUrl = sessionData.homeserverUrl
        val accessToken = sessionData.accessToken
            .takeIf { it.isNotBlank() }
            ?: return Result.failure(IllegalStateException("Missing access token"))

        var lastFailure: Throwable? = null
        for (endpointUrl in buildPreviewEndpoints(homeserverUrl, url, accessToken)) {
            val response = matrixClient.getUrl(endpointUrl)
            if (response.isSuccess) {
                return response.mapCatching { body ->
                    parsePreviewResponse(url, body.decodeToString())
                }
            }

            val failure = response.exceptionOrNull()
            if (failure is ClientException.Generic && failure.message?.contains("404") == true) {
                lastFailure = failure
                continue
            }

            return Result.failure(failure ?: IllegalStateException("Failed to fetch URL preview from $endpointUrl"))
        }

        return Result.failure(lastFailure ?: IllegalStateException("No compatible URL preview endpoint found"))
    }

    private fun parsePreviewResponse(url: String, response: String): UrlPreviewData? {
        val jsonObject = json().parseToJsonElement(response).jsonObject
        val title = jsonObject.stringValue("og:title")
        val description = jsonObject.stringValue("og:description")
        val imageUrl = jsonObject.stringValue("og:image")
        val siteName = jsonObject.stringValue("og:site_name")
        if (title == null && description == null && imageUrl == null) {
            return null
        }
        return UrlPreviewData(
            url = url,
            title = title,
            description = description,
            imageUrl = imageUrl,
            siteName = siteName,
            hostName = hostNameFromUrl(url),
        )
    }
}

private fun buildPreviewEndpoints(homeserverUrl: String, url: String, accessToken: String): List<String> {
    val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8)
    val encodedAccessToken = URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
    val normalizedBase = if (homeserverUrl.endsWith("/")) homeserverUrl else "$homeserverUrl/"
    val baseUri = URI(normalizedBase)
    return listOf(
        "_matrix/client/v1/media/preview_url?url=$encodedUrl&access_token=$encodedAccessToken",
        "_matrix/media/v3/preview_url?url=$encodedUrl&access_token=$encodedAccessToken",
        "_matrix/media/r0/preview_url?url=$encodedUrl&access_token=$encodedAccessToken",
    ).map(baseUri::resolve).map(URI::toString)
}

private fun JsonObject.stringValue(key: String): String? {
    return get(key)
        ?.jsonPrimitive
        ?.contentOrNull
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
}
