/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.sessionstorage.impl.di

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.sessionstorage.impl.SessionDatabase
import io.element.encrypteddb.SqlCipherDriverFactory
import io.element.encrypteddb.passphrase.RandomSecretPassphraseProvider

@BindingContainer
@ContributesTo(AppScope::class)
object SessionStorageModule {
    @Provides
    @SingleIn(AppScope::class)
    fun provideMatrixDatabase(
        @ApplicationContext context: Context,
    ): SessionDatabase {
        val name = "session_database"
        val secretFile = context.getDatabasePath("$name.key")

        // Make sure the parent directory of the key file exists, otherwise it will crash in older Android versions
        val parentDir = secretFile.parentFile
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs()
        }

        val passphraseProvider = RandomSecretPassphraseProvider(context, secretFile)
        val driver = SqlCipherDriverFactory(passphraseProvider)
            .create(SessionDatabase.Schema, "$name.db", context)
        return SessionDatabase(driver)
    }
}
