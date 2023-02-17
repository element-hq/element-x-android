/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.encrypteddb

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import io.element.encrypteddb.passphrase.PassphraseProvider
import net.sqlcipher.database.SupportFactory

class SqlCipherDriverFactory(
    private val passphraseProvider: PassphraseProvider,
) {
    fun create(schema: SqlDriver.Schema, name: String, context: Context): SqlDriver {
        val passphrase = passphraseProvider.getPassphrase()
        val factory = SupportFactory(passphrase)
        return AndroidSqliteDriver(schema = schema, context = context, name = name, factory = factory)
    }
}
