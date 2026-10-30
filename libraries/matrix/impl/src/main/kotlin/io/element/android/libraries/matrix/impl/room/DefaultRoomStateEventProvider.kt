/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomStateEvent
import io.element.android.libraries.matrix.api.room.RoomStateEventProvider
import io.element.android.libraries.network.interceptors.SkipHttpLogging
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

@ContributesBinding(AppScope::class)
class DefaultRoomStateEventProvider(
    private val sessionStore: SessionStore,
    private val okHttpClient: OkHttpClient,
    private val coroutineDispatchers: CoroutineDispatchers,
) : RoomStateEventProvider {
    override suspend fun getStateEvents(
        sessionId: SessionId,
        roomId: RoomId,
        eventType: String,
    ): Result<List<RoomStateEvent>> = withContext(coroutineDispatchers.io) {
        runCatchingExceptions {
            val sessionData = sessionStore.getSession(sessionId.value)
                ?: error("Session not found")
            val url = sessionData.homeserverUrl.toHttpUrl()
                .newBuilder()
                .addPathSegments("_matrix/client/v3/rooms")
                .addPathSegment(roomId.value)
                .addPathSegment("state")
                .build()
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer ${sessionData.accessToken}")
                .tag(SkipHttpLogging::class.java, SkipHttpLogging)
                .get()
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                check(response.isSuccessful) {
                    "Failed to fetch room state: HTTP ${response.code}"
                }
                val responseBody = response.body.string()
                Json.parseToJsonElement(responseBody)
                    .jsonArray
                    .mapNotNull { element ->
                        val event = element.jsonObject
                        val type = event["type"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                        if (type != eventType) return@mapNotNull null
                        val stateKey = event["state_key"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                        val sender = event["sender"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                        val content = event["content"] ?: return@mapNotNull null
                        RoomStateEvent(
                            eventType = type,
                            stateKey = stateKey,
                            sender = UserId(sender),
                            eventId = event["event_id"]?.jsonPrimitive?.contentOrNull?.let(::EventId),
                            timestamp = event["origin_server_ts"]?.jsonPrimitive?.contentOrNull?.toLongOrNull(),
                            contentJson = content.toString(),
                        )
                    }
            }
        }
    }
}
