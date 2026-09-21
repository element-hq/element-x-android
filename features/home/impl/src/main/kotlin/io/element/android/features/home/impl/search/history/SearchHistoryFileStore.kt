/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.search.history

/**
 * Low level, raw byte read/write of the persisted search history.
 *
 * This exists as a seam so that [DefaultSearchHistoryStore] can be unit tested without relying on
 * the Android Keystore. The production implementation ([DefaultSearchHistoryFileStore]) stores the
 * bytes in an encrypted file.
 */
interface SearchHistoryFileStore {
    /**
     * Return the persisted bytes, or `null` if nothing has been persisted yet.
     */
    fun read(): ByteArray?

    /**
     * Persist [bytes], replacing any previously persisted content.
     */
    fun write(bytes: ByteArray)

    /**
     * Delete any persisted content.
     *
     * @return `true` if the persisted content was deleted, `false` if there was no persisted content to delete.
     */
    fun delete(): Boolean
}
