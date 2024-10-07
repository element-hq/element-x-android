/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.sync

import kotlinx.coroutines.flow.StateFlow

interface SyncService {
    /**
     * Tries to start the sync. If already syncing it has no effect.
     */
    suspend fun startSync(): Result<Unit>

    /**
     * Tries to stop the sync. If service is not syncing it has no effect.
     */
    suspend fun stopSync(): Result<Unit>

    /**
     * Flow of [SyncState]. Will be updated as soon as the current [SyncState] changes.
     */
    val syncState: StateFlow<SyncState>
}
