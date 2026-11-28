/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.network

import io.element.android.libraries.matrix.api.network.HttpClient
import io.element.android.libraries.matrix.test.A_HOMESERVER_URL
import io.element.android.libraries.matrix.test.A_SERVER_NAME
import io.element.android.tests.testutils.lambda.lambdaError

class FakeHttpClient(
    override val homeserverUrl: String = A_HOMESERVER_URL,
    override val serverName: String? = A_SERVER_NAME,
    private val getUrlResult: (String) -> Result<ByteArray> = { lambdaError() },
) : HttpClient {
    override suspend fun getUrl(url: String): Result<ByteArray> = getUrlResult(url)
}
