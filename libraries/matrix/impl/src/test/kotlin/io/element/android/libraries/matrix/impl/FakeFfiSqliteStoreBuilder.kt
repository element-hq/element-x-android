/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import org.matrix.rustcomponents.sdk.NoHandle
import org.matrix.rustcomponents.sdk.SqliteStoreBuilder

class FakeFfiSqliteStoreBuilder : SqliteStoreBuilder(NoHandle) {
    override fun cacheSize(cacheSize: UInt?): SqliteStoreBuilder = this
    override fun journalSizeLimit(limit: UInt?): SqliteStoreBuilder = this
    override fun passphrase(passphrase: String?): SqliteStoreBuilder = this
    override fun poolMaxSize(poolMaxSize: UInt?): SqliteStoreBuilder = this
    override fun systemIsMemoryConstrained(): SqliteStoreBuilder = this
}
