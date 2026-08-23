/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.search

import kotlinx.coroutines.flow.Flow

/**
 * Durable state for the message search backfill.
 *
 * Kept apart from `SessionPreferencesStore`: that holds user-facing settings, this holds machine
 * state with a different lifetime, which must be independently resettable without touching anything
 * the user chose.
 */
interface SearchBackfillStore {
    /** Emits the current cursor, or null when no sweep has ever been recorded. */
    fun cursorFlow(): Flow<SearchBackfillCursor?>

    /** Reads the cursor once. Returns null if absent or unreadable — never throws. */
    suspend fun getCursor(): SearchBackfillCursor?

    suspend fun setCursor(cursor: SearchBackfillCursor)

    suspend fun clear()
}
