/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.network

import io.element.android.libraries.matrix.api.network.HttpClient

class FakeHttpClientFactory(
    private val createResult: (homeserverUrl: String, serverName: String?) -> HttpClient = { _, _ -> FakeHttpClient() },
) : HttpClient.Factory {
    override fun create(homeserverUrl: String, serverName: String?): HttpClient = createResult(homeserverUrl, serverName)
}
