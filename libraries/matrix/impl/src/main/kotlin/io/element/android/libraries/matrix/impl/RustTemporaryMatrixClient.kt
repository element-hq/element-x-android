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
import io.element.android.libraries.matrix.api.paths.SessionPaths
import io.element.android.libraries.matrix.impl.exception.mapClientException
import org.matrix.rustcomponents.sdk.Client

class RustTemporaryMatrixClient(
    private val client: Client,
    private val paths: SessionPaths?,
) : TemporaryMatrixClient {
    override suspend fun getUrl(url: String): Result<ByteArray> = runCatchingExceptions {
        client.getUrl(url)
    }.mapFailure { it.mapClientException() }

    override fun close() {
        client.close()
        paths?.let {
            if (it.fileDirectory.exists()) {
                it.fileDirectory.deleteRecursively()
            }
            if (it.cacheDirectory.exists()) {
                it.cacheDirectory.deleteRecursively()
            }
        }
    }
}
