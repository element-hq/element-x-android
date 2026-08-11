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
import io.element.android.libraries.matrix.api.TemporaryMatrixClient
import io.element.android.libraries.matrix.api.TemporaryMatrixClientFactory
import io.element.android.libraries.matrix.impl.paths.SessionPathsFactory

@ContributesBinding(AppScope::class)
class RustTemporaryMatrixClientFactory(
    private val sessionPathsFactory: SessionPathsFactory,
    private val rustMatrixClientFactory: RustMatrixClientFactory,
) : TemporaryMatrixClientFactory {
    override suspend fun create(homeServerUrl: String): Result<TemporaryMatrixClient> {
        return runCatchingExceptions {
            val sessionPaths = sessionPathsFactory.create()
            val client = rustMatrixClientFactory.getBaseClientBuilder(
                sessionPaths = sessionPaths,
                clientSecret = null,
                slidingSyncType = ClientBuilderSlidingSync.Native,
                isMessageSearchAvailable = false,
            )
                .homeserverUrl(homeServerUrl)
                .build()
            RustTemporaryMatrixClient(client, sessionPaths)
        }
    }
}
