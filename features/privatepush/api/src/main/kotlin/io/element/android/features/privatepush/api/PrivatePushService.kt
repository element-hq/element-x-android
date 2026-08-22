/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.api

import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for the private-notifications state, shared by the FTUE step,
 * the "No distributors available" routing and the Settings entry.
 */
interface PrivatePushService {
    /** Current state, computed from installed apps + the registered push config. Cheap (prefs + PackageManager). */
    suspend fun status(sessionId: SessionId): PrivatePushStatus

    /** true when the FTUE step should be shown: enabled && status != Private && !dismissed. */
    suspend fun shouldShowSetup(sessionId: SessionId): Boolean

    /** "Later" flag, persisted per session. */
    fun isDismissed(sessionId: SessionId): Flow<Boolean>

    suspend fun setDismissed(sessionId: SessionId, dismissed: Boolean)

    /** Forced (re)display, used where the generic "No distributors available" dialog used to appear. */
    fun setupRequested(sessionId: SessionId): Flow<Boolean>

    fun requestSetup(sessionId: SessionId)

    fun clearSetupRequest(sessionId: SessionId)
}
