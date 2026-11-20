/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import io.element.android.libraries.core.extensions.mapFailure
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.TemporaryMatrixClient
import io.element.android.libraries.matrix.impl.exception.mapClientException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.Client

class RustTemporaryMatrixClient(
    private val client: Client,
    private val dispatcher: CoroutineDispatcher,
    // Invoked once the client is closed, for instance to delete its session directories
    private val onClose: () -> Unit = {},
) : TemporaryMatrixClient {
    override suspend fun getUrl(url: String): Result<ByteArray> = withContext(dispatcher) {
        runCatchingExceptions {
            client.getUrl(url)
        }.mapFailure { it.mapClientException() }
    }

    override val server: String? = client.server()
    override val homeserverUrl: String = client.homeserver()

    override fun close() {
        client.close()
        onClose()
    }
}
