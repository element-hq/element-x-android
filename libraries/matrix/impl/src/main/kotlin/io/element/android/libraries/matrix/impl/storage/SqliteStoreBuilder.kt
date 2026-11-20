/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.storage

import android.util.Base64
import io.element.android.libraries.androidutils.crypto.ClientSecret
import io.element.android.libraries.core.data.ByteUnit
import io.element.android.libraries.core.data.megaBytes
import io.element.android.libraries.matrix.api.paths.SessionPaths
import org.matrix.rustcomponents.sdk.ClientBuilder
import uniffi.matrix_sdk_sqlite.Base64Variant
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
            is ClientSecret.Passphrase -> {
                // Use high entropy passphrase to migrate a passphrase to a raw key.
                val base64Passphrase = Base64.decode(clientSecret.value, Base64.NO_WRAP or Base64.NO_PADDING)
                inner = inner.highEntropyPassphrase(base64Passphrase, Base64Variant.UNPADDED)
                // Once the passphrase is migrated, we can derive a raw key from it and use that for the SDK, replacing the line above.
//                inner = inner.key(base64Passphrase)
            }
            is ClientSecret.RawKey -> {
                inner = inner.key(clientSecret.bytes)
            }
        }
        return this
    }

    override fun setupClientBuilder(clientBuilder: ClientBuilder): ClientBuilder {
        return clientBuilder.sqliteStore(this.inner)
    }
}
