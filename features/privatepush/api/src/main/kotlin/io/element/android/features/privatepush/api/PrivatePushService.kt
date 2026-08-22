/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.api

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for the private-notifications state, shared by the Settings entry
 * (the only place the ntfy flow is reachable from) and the "No distributors available" fallback.
 * The ntfy setup page is never shown automatically.
 */
interface PrivatePushService {
    /** Current state, computed from installed apps + the registered push config. Cheap (prefs + PackageManager). */
    suspend fun status(sessionId: SessionId): PrivatePushStatus

    /**
     * Silently register the built-in Feral provider (no distributor app needed), used when the stored
     * provider has no usable distributor (ntfy uninstalled or never configured). Also updates the stored
     * provider name. Returns false when the built-in provider is unavailable or the registration failed:
     * only then is the ntfy flow worth showing.
     */
    suspend fun fallBackToBuiltIn(matrixClient: MatrixClient): Boolean

    /** "Later" flag, persisted per session. */
    fun isDismissed(sessionId: SessionId): Flow<Boolean>

    suspend fun setDismissed(sessionId: SessionId, dismissed: Boolean)

    /** Flow bookkeeping ("connect" requested for a session); nothing displays the flow automatically any more. */
    fun setupRequested(sessionId: SessionId): Flow<Boolean>

    fun requestSetup(sessionId: SessionId)

    fun clearSetupRequest(sessionId: SessionId)
}
