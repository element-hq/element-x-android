/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.encrypteddb

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.element.encrypteddb.passphrase.PassphraseProvider
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

/**
 * Creates an encrypted version of the [SqlDriver] using SQLCipher's [SupportOpenHelperFactory].
 * @param passphraseProvider Provides the passphrase needed to use the SQLite database with SQLCipher.
 */
class SqlCipherDriverFactory(
    private val passphraseProvider: PassphraseProvider,
) {
    companion object {
        init {
            System.loadLibrary("sqlcipher")
        }
    }

    /**
     * Returns a valid [SqlDriver] with SQLCipher support.
     * @param schema The SQLite DB schema.
     * @param name The name of the database to create.
     * @param context Android [Context], used to instantiate the driver.
     * @param onUpgradeCallback Optional callback to handle database upgrades, which will be called in the [AndroidSqliteDriver.Callback.onUpgrade] method.
     */
    fun create(
        schema: SqlSchema<QueryResult.Value<Unit>>,
        name: String,
        context: Context,
        onUpgradeCallback: ((driver: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) -> Unit)? = null,
    ): SqlDriver {
        val passphrase = passphraseProvider.getPassphrase()
        val factory = if (passphrase.size == 32) {
            // Raw SQLCipher keys are 32 bytes long
            val key = "x'${passphrase.toHexString()}'".toByteArray()
            SupportOpenHelperFactory(key)
        } else {
            // Usual passphrase, which will be processed by SQLCipher's internal key derivation function
            SupportOpenHelperFactory(passphrase)
        }
        return AndroidSqliteDriver(schema = schema, context = context, name = name, factory = factory, callback = object : AndroidSqliteDriver.Callback(
                schema
            ) {
            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                onUpgradeCallback?.invoke(db, oldVersion, newVersion)
            }
        })
    }
}
