/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.sessionstorage.impl

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.cachestore.api.CacheData
import io.element.android.libraries.cachestore.impl.CacheDatabase
import io.element.android.libraries.cachestore.impl.DatabaseCacheStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import io.element.android.libraries.cachestore.CacheData as DbCacheData

private const val A_KEY = "aKey"
private const val A_DATA_1 = "aData1"
private const val A_DATA_2 = "aData2"

class DatabaseCacheStoreTest {
    private lateinit var database: CacheDatabase
    private lateinit var databaseCacheStore: DatabaseCacheStore

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        // Initialise in memory SQLite driver
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        CacheDatabase.Schema.create(driver)

        database = CacheDatabase(driver)
        databaseCacheStore = DatabaseCacheStore(
            database = database,
        )
    }

    @Test
    fun `storeData persists the CacheData into the DB, deleteData deletes it`() = runTest {
        // Assert that no data is stored for the key
        assertThat(database.cacheDataQueries.selectData(A_KEY).executeAsOneOrNull()).isNull()
        // Store data
        databaseCacheStore.storeData(A_KEY, CacheData(A_DATA_1, 1))
        assertThat(database.cacheDataQueries.selectData(A_KEY).executeAsOneOrNull()).isEqualTo(
            DbCacheData(
                key = A_KEY,
                value_ = A_DATA_1,
                updatedAt = 1,
            )
        )
        // Update data
        databaseCacheStore.storeData(A_KEY, CacheData(A_DATA_2, 2))
        assertThat(database.cacheDataQueries.selectData(A_KEY).executeAsOneOrNull()).isEqualTo(
            DbCacheData(
                key = A_KEY,
                value_ = A_DATA_2,
                updatedAt = 2,
            )
        )
        // Delete data
        databaseCacheStore.deleteData(A_KEY)
        assertThat(database.cacheDataQueries.selectData(A_KEY).executeAsOneOrNull()).isNull()
    }

    @Test
    fun `deleteAll deletes all the data`() = runTest {
        // Assert that no data is stored for the key
        assertThat(database.cacheDataQueries.selectData(A_KEY).executeAsOneOrNull()).isNull()
        // Store data
        databaseCacheStore.storeData(A_KEY, CacheData(A_DATA_1, 1))
        assertThat(database.cacheDataQueries.selectData(A_KEY).executeAsOneOrNull()).isEqualTo(
            DbCacheData(
                key = A_KEY,
                value_ = A_DATA_1,
                updatedAt = 1,
            )
        )
        // Delete all data
        databaseCacheStore.deleteAll()
        assertThat(database.cacheDataQueries.selectData(A_KEY).executeAsOneOrNull()).isNull()
    }
}
