/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.uri.ensureProtocol
import io.element.android.libraries.matrix.api.TemporaryMatrixClient
import io.element.android.libraries.matrix.api.TemporaryMatrixClientFactory
import io.element.android.libraries.matrix.impl.paths.SessionPathsFactory
import java.net.URL

@ContributesBinding(AppScope::class)
class RustTemporaryMatrixClientFactory(
    private val sessionPathsFactory: SessionPathsFactory,
    private val rustMatrixClientFactory: RustMatrixClientFactory,
) : TemporaryMatrixClientFactory {
    override suspend fun create(serverName: String): Result<TemporaryMatrixClient> {
        return runCatchingExceptions {
            // In case the 'serverName' is a full URL, we need to extract the host and port to pass to the client builder.
            val parsedUrl = URL(serverName.ensureProtocol())
            val domain = parsedUrl.host ?: error("Invalid server name: $serverName")
            val port = parsedUrl.port
            val formattedServerName = if (port != -1) "$domain:$port" else domain

            val sessionPaths = sessionPathsFactory.create()
            val client = rustMatrixClientFactory.getBaseClientBuilder(
                sessionPaths = sessionPaths,
                clientSecret = null,
                slidingSyncType = ClientBuilderSlidingSync.Native,
                isMessageSearchAvailable = false,
            )
                .serverName(formattedServerName)
                .build()
            RustTemporaryMatrixClient(client, sessionPaths)
        }
    }
}
