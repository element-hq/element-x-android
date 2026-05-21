/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.storage

import io.element.android.libraries.core.data.ByteUnit
import io.element.android.libraries.core.data.megaBytes
import io.element.android.libraries.matrix.impl.ClientSecret
import io.element.android.libraries.matrix.impl.paths.SessionPaths
import org.matrix.rustcomponents.sdk.ClientBuilder
import org.matrix.rustcomponents.sdk.SqliteStoreBuilder as SdkSqliteStoreBuilder

/**
 * Abstraction over the SDK's [SdkSqliteStoreBuilder] to allow configuring it with a ClientSecret and to hide the SDK from the rest of the codebase.
 */
interface SqliteStoreBuilder {
    /**
     * Configure the builder with a [ClientSecret], if provided. If the [clientSecret] is null, the databases will not be encrypted.
     */
    fun secret(clientSecret: ClientSecret?): SqliteStoreBuilder

    /**
     * Configure the provided [clientBuilder] with the configured [SdkSqliteStoreBuilder] and return it.
     */
    fun setupClientBuilder(clientBuilder: ClientBuilder): ClientBuilder
}

class RustSqliteStoreBuilder(
    sessionPaths: SessionPaths,
) : SqliteStoreBuilder {
    private var inner = SdkSqliteStoreBuilder(
        dataPath = sessionPaths.fileDirectory.absolutePath,
        cachePath = sessionPaths.cacheDirectory.absolutePath,
    ).journalSizeLimit(25.megaBytes.into(ByteUnit.BYTES).toUInt())

    override fun secret(clientSecret: ClientSecret?): SqliteStoreBuilder {
        when (clientSecret) {
            null -> Unit
            is ClientSecret.Passphrase -> inner = inner.passphrase(clientSecret.value)
            is ClientSecret.RawKey -> {
                // Ensure the key is 32 bytes long, as required by the SDK
                inner = inner.key(clientSecret.keyOfSize(32))
            }
        }
        return this
    }

    override fun setupClientBuilder(clientBuilder: ClientBuilder): ClientBuilder {
        return clientBuilder.sqliteStore(this.inner)
    }
}

private fun ClientSecret.RawKey.keyOfSize(size: Int): ByteArray {
    return if (bytes.size == 32) {
        bytes
    } else if (bytes.size < 32) {
        // If the key is shorter than 32 bytes, pad it with zeros
        bytes + ByteArray(32 - bytes.size)
    } else {
        // Otherwise, take the first 32 bytes of the key
        bytes.copyOfRange(0, 32)
    }
}
