/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.search.history

/**
 * In-memory [SearchHistoryFileStore] used to test [DefaultSearchHistoryStore] without the Android Keystore.
 */
class FakeSearchHistoryFileStore(
    initialContent: ByteArray? = null,
) : SearchHistoryFileStore {
    var content: ByteArray? = initialContent
        private set

    var writeCount = 0
        private set

    override fun read(): ByteArray? = content

    override fun write(bytes: ByteArray) {
        content = bytes
        writeCount++
    }

    override fun delete(): Boolean {
        content = null
        return true
    }
}
