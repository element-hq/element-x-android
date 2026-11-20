/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.encrypteddb

import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.Test

class MigrationCallbackTest {
    @Test
    fun `onUpgrade migrates the schema`() {
        val schema = RecordingSchema()
        MigrationCallback(schema, onUpgradeCallback = null).onUpgrade(mockk(relaxed = true), 9, 11)
        assertThat(schema.migrations).containsExactly(9L to 11L)
    }

    @Test
    fun `onUpgrade replaces the database key before migrating the schema`() {
        val order = mutableListOf<String>()
        val schema = RecordingSchema(onMigrate = { order.add("migrate") })
        MigrationCallback(schema, onUpgradeCallback = { _, _, _ -> order.add("rekey") })
            .onUpgrade(mockk(relaxed = true), 9, 11)
        assertThat(order).containsExactly("rekey", "migrate").inOrder()
    }
}

private class RecordingSchema(
    private val onMigrate: () -> Unit = {},
) : SqlSchema<QueryResult.Value<Unit>> {
    val migrations = mutableListOf<Pair<Long, Long>>()

    override val version = 11L

    override fun create(driver: SqlDriver) = QueryResult.Unit

    override fun migrate(
        driver: SqlDriver,
        oldVersion: Long,
        newVersion: Long,
        vararg callbacks: AfterVersion,
    ): QueryResult.Value<Unit> {
        migrations.add(oldVersion to newVersion)
        onMigrate()
        return QueryResult.Unit
    }
}
