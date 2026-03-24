/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.search

import android.annotation.SuppressLint
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.search.RoomMessageSearchResult
import io.element.android.libraries.matrix.api.search.RoomMessageSearchResultItem
import io.element.android.libraries.matrix.api.search.RoomMessageSearchService
import io.element.android.libraries.matrix.api.search.SearchOrder
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber

@ContributesBinding(SessionScope::class)
class RustRoomMessageSearchService(
    private val sessionId: SessionId,
    private val sessionStore: SessionStore,
    private val okHttpClient: OkHttpClient,
    private val dispatchers: CoroutineDispatchers,
) : RoomMessageSearchService {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    @SuppressLint("NewApi") // False positive: lint confuses Map.get(String) with MatchGroupCollection.get(String)
    override suspend fun search(
        roomId: RoomId,
        searchTerm: String,
        batchSize: Int,
        nextBatchToken: String?,
        orderBy: SearchOrder,
    ): Result<RoomMessageSearchResult> = withContext(dispatchers.io) {
        runCatchingExceptions {
            val sessionData = sessionStore.getSession(sessionId.value)
                ?: error("No session data found for $sessionId")

            val homeserverUrl = sessionData.homeserverUrl.trimEnd('/')
            val accessToken = sessionData.accessToken

            val requestBody = SearchRequestBody(
                searchCategories = SearchCategories(
                    roomEvents = RoomEventsSearchCriteria(
                        searchTerm = searchTerm,
                        filter = RoomEventsFilter(rooms = listOf(roomId.value)),
                        orderBy = when (orderBy) {
                            SearchOrder.RECENT -> "recent"
                            SearchOrder.RANK -> "rank"
                        },
                        limit = batchSize,
                    )
                )
            )

            val jsonBody = json.encodeToString(SearchRequestBody.serializer(), requestBody)
            Timber.d("Search request: POST $homeserverUrl/_matrix/client/v3/search body=$jsonBody")

            val urlBuilder = StringBuilder("$homeserverUrl/_matrix/client/v3/search")
            if (nextBatchToken != null) {
                urlBuilder.append("?next_batch=$nextBatchToken")
            }

            val request = Request.Builder()
                .url(urlBuilder.toString())
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .header("Authorization", "Bearer $accessToken")
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                error("Search request failed with HTTP ${response.code}: ${response.body.string()}")
            }

            val responseBodyString = response.body.string()
            Timber.d("Search response: HTTP ${response.code}, body=$responseBodyString")

            val searchResponse = json.decodeFromString(SearchResponseBody.serializer(), responseBodyString)
            val roomEventsResponse = searchResponse.searchCategories.roomEvents

            RoomMessageSearchResult(
                results = roomEventsResponse?.results?.mapNotNull { item ->
                    val event = item.result ?: return@mapNotNull null
                    val eventId = event.eventId ?: return@mapNotNull null
                    val sender = event.sender ?: return@mapNotNull null
                    val body = event.content?.body ?: return@mapNotNull null

                    val senderProfile = item.context?.profileInfo?.get(sender)

                    RoomMessageSearchResultItem(
                        eventId = EventId(eventId),
                        senderId = UserId(sender),
                        senderDisplayName = senderProfile?.displayName,
                        senderAvatarUrl = senderProfile?.avatarUrl,
                        body = body,
                        timestamp = event.originServerTs ?: 0L,
                    )
                }.orEmpty(),
                count = roomEventsResponse?.count,
                highlights = roomEventsResponse?.highlights.orEmpty(),
                nextBatchToken = roomEventsResponse?.nextBatch,
            )
        }.onFailure {
            Timber.e(it, "Failed to search messages in room $roomId")
        }
    }
}
