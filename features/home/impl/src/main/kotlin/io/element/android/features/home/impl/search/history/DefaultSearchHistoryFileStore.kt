/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.search.history

import android.content.Context
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.encrypteddb.crypto.EncryptedFile
import java.io.File

private const val SEARCH_HISTORY_FILE_NAME = "search_history"

/**
 * Persists the search history in an encrypted file inside the current session file directory, using
 * [EncryptedFile] (Tink StreamingAead + a master key stored in the Android Keystore).
 */
@ContributesBinding(SessionScope::class)
class DefaultSearchHistoryFileStore(
    @ApplicationContext private val context: Context,
    matrixClient: MatrixClient,
) : SearchHistoryFileStore {
    private val file: File by lazy {
        File(matrixClient.sessionPaths.fileDirectory, SEARCH_HISTORY_FILE_NAME)
    }

    override fun read(): ByteArray? {
        if (!file.exists()) return null
        return EncryptedFile(context, file).openFileInput().use { it.readBytes() }
    }

    override fun write(bytes: ByteArray) {
        file.parentFile?.takeIf { !it.exists() }?.mkdirs()
        // Opening a FileOutputStream truncates any previous content, so this overwrites the file.
        EncryptedFile(context, file).openFileOutput().use { it.write(bytes) }
    }

    override fun delete(): Boolean {
        return file.delete()
    }
}
