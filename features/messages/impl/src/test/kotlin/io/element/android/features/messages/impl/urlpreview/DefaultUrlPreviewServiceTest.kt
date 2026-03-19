/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.urlpreview

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.androidutils.json.DefaultJsonProvider
import io.element.android.libraries.matrix.api.exception.ClientException
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultUrlPreviewServiceTest {
    @Test
    fun `get preview parses matrix preview response`() = runTest {
        val sessionStore = InMemorySessionStore(
            initialList = listOf(
                aSessionData().copy(homeserverUrl = "https://matrix.example.org")
            )
        )
        val service = DefaultUrlPreviewService(
            matrixClient = FakeMatrixClient(
                getUrlLambda = { requestedUrl ->
                    assertThat(requestedUrl)
                        .isEqualTo(
                            "https://matrix.example.org/_matrix/client/v1/media/preview_url?" +
                                "url=https%3A%2F%2Fexample.org%2Farticle&access_token=anAccessToken"
                        )
                    Result.success(
                        """
                        {
                          "og:title": "Example article",
                          "og:description": "Preview description",
                          "og:image": "https://cdn.example.org/thumb.jpg",
                          "og:site_name": "Example"
                        }
                        """.trimIndent().encodeToByteArray()
                    )
                },
            ),
            sessionStore = sessionStore,
            json = DefaultJsonProvider(),
        )

        val preview = service.getPreview("https://example.org/article").getOrNull()

        assertThat(preview).isEqualTo(
            UrlPreviewData(
                url = "https://example.org/article",
                title = "Example article",
                description = "Preview description",
                imageUrl = "https://cdn.example.org/thumb.jpg",
                siteName = "Example",
                hostName = "example.org",
            )
        )
    }

    @Test
    fun `get preview falls back to media v3 when client v1 endpoint is unavailable`() = runTest {
        val sessionStore = InMemorySessionStore(
            initialList = listOf(
                aSessionData().copy(homeserverUrl = "https://matrix.example.org")
            )
        )
        val requestedUrls = mutableListOf<String>()
        val service = DefaultUrlPreviewService(
            matrixClient = FakeMatrixClient(
                getUrlLambda = { requestedUrl ->
                    requestedUrls += requestedUrl
                    when (requestedUrl) {
                        "https://matrix.example.org/_matrix/client/v1/media/preview_url?" +
                            "url=https%3A%2F%2Fexample.org%2Farticle&access_token=anAccessToken" -> {
                            Result.failure(ClientException.Generic("404 Not Found", null))
                        }
                        "https://matrix.example.org/_matrix/media/v3/preview_url?" +
                            "url=https%3A%2F%2Fexample.org%2Farticle&access_token=anAccessToken" -> {
                            Result.success(
                                """
                                {
                                  "og:title": "Example article"
                                }
                                """.trimIndent().encodeToByteArray()
                            )
                        }
                        else -> error("Unexpected URL: $requestedUrl")
                    }
                },
            ),
            sessionStore = sessionStore,
            json = DefaultJsonProvider(),
        )

        val preview = service.getPreview("https://example.org/article").getOrNull()

        assertThat(requestedUrls).containsExactly(
            "https://matrix.example.org/_matrix/client/v1/media/preview_url?" +
                "url=https%3A%2F%2Fexample.org%2Farticle&access_token=anAccessToken",
            "https://matrix.example.org/_matrix/media/v3/preview_url?" +
                "url=https%3A%2F%2Fexample.org%2Farticle&access_token=anAccessToken",
        ).inOrder()
        assertThat(preview).isEqualTo(
            UrlPreviewData(
                url = "https://example.org/article",
                title = "Example article",
                description = null,
                imageUrl = null,
                siteName = null,
                hostName = "example.org",
            )
        )
    }
}
