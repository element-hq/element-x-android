/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.api.store

import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.CoroutineScope

/**
 * Caches one [SessionPreferencesStore] per session, so that every caller of a given session shares the same instance.
 */
interface SessionPreferencesStoreFactory {
    /**
     * Returns the store of a session, creating it on first use.
     *
     * @param sessionId the session whose store is requested.
     * @param sessionCoroutineScope the scope the store uses for its own work, only honoured when the store is actually created.
     */
    fun get(sessionId: SessionId, sessionCoroutineScope: CoroutineScope): SessionPreferencesStore

    /**
     * Drops the cached store of a session, to be called when that session goes away.
     *
     * @param sessionId the session whose store is dropped.
     */
    fun remove(sessionId: SessionId)
}
