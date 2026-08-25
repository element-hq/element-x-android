/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.sessionstorage.api.observer

/**
 * Notifies interested components when a session is created or deleted, so they can set up or clean up their own per-session data.
 */
interface SessionObserver {
    /**
     * @param listener the listener to notify from now on.
     */
    fun addListener(listener: SessionListener)

    /**
     * @param listener the listener to stop notifying.
     */
    fun removeListener(listener: SessionListener)
}
