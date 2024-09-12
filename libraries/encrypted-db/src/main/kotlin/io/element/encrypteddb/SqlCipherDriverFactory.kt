/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.encrypteddb

import android.content.Context
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.element.encrypteddb.passphrase.PassphraseProvider
import net.sqlcipher.database.SupportFactory

/**
 * Creates an encrypted version of the [SqlDriver] using SQLCipher's [SupportFactory].
 * @param passphraseProvider Provides the passphrase needed to use the SQLite database with SQLCipher.
 */
class SqlCipherDriverFactory(
    private val passphraseProvider: PassphraseProvider,
) {
    /**
     * Returns a valid [SqlDriver] with SQLCipher support.
     * @param schema The SQLite DB schema.
     * @param name The name of the database to create.
     * @param context Android [Context], used to instantiate the driver.
     */
    fun create(schema: SqlSchema<QueryResult.Value<Unit>>, name: String, context: Context): SqlDriver {
        val passphrase = passphraseProvider.getPassphrase()
        val factory = SupportFactory(passphrase)
        return AndroidSqliteDriver(schema = schema, context = context, name = name, factory = factory)
    }
}
