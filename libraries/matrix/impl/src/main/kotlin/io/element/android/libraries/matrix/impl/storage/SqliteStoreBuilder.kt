/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.storage

import io.element.android.libraries.matrix.impl.paths.SessionPaths
import org.matrix.rustcomponents.sdk.ClientBuilder
import org.matrix.rustcomponents.sdk.SqliteStoreBuilder as SdkSqliteStoreBuilder

interface SqliteStoreBuilder {
    fun passphrase(passphrase: String?): SqliteStoreBuilder
    fun setupClientBuilder(clientBuilder: ClientBuilder): ClientBuilder
}

class RustSqliteStoreBuilder(
    private val sessionPaths: SessionPaths,
) : SqliteStoreBuilder {
    private var inner = SdkSqliteStoreBuilder(
        dataPath = sessionPaths.fileDirectory.absolutePath,
        cachePath = sessionPaths.cacheDirectory.absolutePath,
    )

    override fun passphrase(passphrase: String?): SqliteStoreBuilder {
        inner = inner.passphrase(passphrase)
        return this
    }

    override fun setupClientBuilder(clientBuilder: ClientBuilder): ClientBuilder {
        return clientBuilder.sqliteStore(this.inner)
    }
}
