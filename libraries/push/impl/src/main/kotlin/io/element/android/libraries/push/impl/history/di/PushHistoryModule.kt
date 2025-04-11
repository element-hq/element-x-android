/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.history.di

import android.content.Context
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.push.impl.PushDatabase
import io.element.encrypteddb.SqlCipherDriverFactory
import io.element.encrypteddb.passphrase.RandomSecretPassphraseProvider

@Module
@ContributesTo(AppScope::class)
object PushHistoryModule {
    @Provides
    @SingleIn(AppScope::class)
    fun providePushDatabase(
        @ApplicationContext context: Context,
    ): PushDatabase {
        val name = "push_database"
        val secretFile = context.getDatabasePath("$name.key")

        // Make sure the parent directory of the key file exists, otherwise it will crash in older Android versions
        val parentDir = secretFile.parentFile
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs()
        }

        val passphraseProvider = RandomSecretPassphraseProvider(context, secretFile)
        val driver = SqlCipherDriverFactory(passphraseProvider)
            .create(PushDatabase.Schema, "$name.db", context)
        return PushDatabase(driver)
    }
}
