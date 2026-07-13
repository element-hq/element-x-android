/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.encrypteddb.utils

import androidx.sqlite.db.SupportSQLiteDatabase
import io.element.encrypteddb.passphrase.RandomDatabaseSecretProvider
import timber.log.Timber

/**
 * A utility class to replace the encryption key of an existing SQLCipher database.
 * This is used during database migrations when we want to change the encryption key.
 *
 * @param databaseSecretProvider The provider for generating new secrets.
 */
class ReplaceDatabaseKey(
    private val databaseSecretProvider: RandomDatabaseSecretProvider
) {
    fun replaceKey(name: String, database: SupportSQLiteDatabase) {
        Timber.d("Re-keying database $name")
        // Reset the passphrase provider to generate a new passphrase
        databaseSecretProvider.reset()

        // Get the new secret and convert it to the format expected by SQLCipher
        val newSecret = databaseSecretProvider.getSecret()
        val key = newSecret.formattedAsString()

        // Use the PRAGMA rekey command to change the encryption key of the database
        database.query("PRAGMA rekey = \"$key\";").close()

        // Verify that the database can be accessed with the new key by running a simple query
        val result = database.query("select count(*) from sqlite_master").use { cursor ->
            if (cursor.moveToNext()) cursor.getLong(0) else -1L
        }
        if (result >= 0) {
            Timber.d("Re-keying database $name completed")
        } else {
            Timber.e("Re-keying database $name didn't work as expected.")
        }
    }
}
