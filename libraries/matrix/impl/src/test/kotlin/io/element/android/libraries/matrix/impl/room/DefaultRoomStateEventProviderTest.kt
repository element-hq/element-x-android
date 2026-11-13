/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomStateEvent
import io.element.android.libraries.network.interceptors.SkipHttpLogging
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.Test

class DefaultRoomStateEventProviderTest {
    @Test
    fun `get state events filters by type and preserves event identity`() = runTest {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(
                MockResponse(
                    body = """
                        [
                          {
                            "type": "im.vector.modular.widgets",
                            "state_key": "calendar",
                            "sender": "@alice:example.org",
                            "event_id": "${'$'}widget-event",
                            "origin_server_ts": 1234,
                            "content": {
                              "type": "m.custom",
                              "url": "https://widget.example/calendar"
                            }
                          },
                          {
                            "type": "m.room.name",
                            "state_key": "",
                            "sender": "@alice:example.org",
                            "event_id": "${'$'}name-event",
                            "origin_server_ts": 1235,
                            "content": {
                              "name": "Room"
                            }
                          }
                        ]
                    """.trimIndent(),
                )
            )
            val sessionId = UserId("@alice:example.org")
            val roomId = RoomId("!room:example.org")
            val sessionStore = InMemorySessionStore(
                initialList = listOf(
                    aSessionData(sessionId = sessionId.value).copy(
                        homeserverUrl = server.url("/").toString(),
                        accessToken = "secret-token",
                    )
                )
            )
            var skippedHttpLogging = false
            val httpClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    skippedHttpLogging = chain.request().tag(SkipHttpLogging::class.java) != null
                    chain.proceed(chain.request())
                }
                .build()
            val provider = DefaultRoomStateEventProvider(
                sessionStore = sessionStore,
                okHttpClient = httpClient,
                coroutineDispatchers = testCoroutineDispatchers(),
            )

            val result = provider.getStateEvents(
                sessionId = sessionId,
                roomId = roomId,
                eventType = "im.vector.modular.widgets",
            ).getOrThrow()

            assertThat(result).containsExactly(
                RoomStateEvent(
                    eventType = "im.vector.modular.widgets",
                    stateKey = "calendar",
                    sender = UserId("@alice:example.org"),
                    eventId = EventId("\$widget-event"),
                    timestamp = 1234,
                    contentJson = """{"type":"m.custom","url":"https://widget.example/calendar"}""",
                )
            )

            val request = server.takeRequest()
            assertThat(skippedHttpLogging).isTrue()
            assertThat(request.headers["Authorization"]).isEqualTo("Bearer secret-token")
            assertThat(request.url.pathSegments).containsExactly(
                "_matrix",
                "client",
                "v3",
                "rooms",
                "!room:example.org",
                "state",
            ).inOrder()
        } finally {
            server.close()
        }
    }

    @Test
    fun `get state events returns failure for unsuccessful response`() = runTest {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(MockResponse(code = 403, body = "{}"))
            val sessionId = UserId("@alice:example.org")
            val sessionStore = InMemorySessionStore(
                initialList = listOf(
                    aSessionData(sessionId = sessionId.value).copy(
                        homeserverUrl = server.url("/").toString(),
                        accessToken = "secret-token",
                    )
                )
            )
            val provider = DefaultRoomStateEventProvider(
                sessionStore = sessionStore,
                okHttpClient = OkHttpClient(),
                coroutineDispatchers = testCoroutineDispatchers(),
            )

            val result = provider.getStateEvents(
                sessionId = sessionId,
                roomId = RoomId("!room:example.org"),
                eventType = "im.vector.modular.widgets",
            )

            assertThat(result.isFailure).isTrue()
        } finally {
            server.close()
        }
    }
}
