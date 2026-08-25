/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.sessionstorage.api.observer

/**
 * Reacts to sessions being created or deleted; both methods have an empty default so a listener only overrides what it cares about.
 */
interface SessionListener {
    /**
     * Called after a session has been stored, for instance following a login.
     *
     * @param userId the session that was created.
     */
    suspend fun onSessionCreated(userId: String) {}

    /**
     * Called after a session has been removed, which is where per-session data should be cleaned up.
     *
     * @param userId the session that was deleted.
     * @param wasLastSession true when no session remains, so app-wide data can be cleaned up too.
     */
    suspend fun onSessionDeleted(userId: String, wasLastSession: Boolean) {}
}
