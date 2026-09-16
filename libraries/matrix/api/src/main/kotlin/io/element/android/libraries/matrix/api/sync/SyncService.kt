/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.sync

import kotlinx.coroutines.flow.StateFlow

/**
 * Drives the sync loop of a single session and exposes its current state.
 *
 * Both [startSync] and [stopSync] are idempotent and never throw: any error is wrapped in the returned [Result].
 */
interface SyncService {
    /**
     * Tries to start the sync. If already syncing, or if the service has been destroyed, it has no effect.
     */
    suspend fun startSync(): Result<Unit>

    /**
     * Tries to stop the sync. If the service is not syncing, or has been destroyed, it has no effect.
     */
    suspend fun stopSync(): Result<Unit>

    /**
     * Flow of [SyncState]. Will be updated as soon as the current [SyncState] changes.
     * Starts at [SyncState.Idle] and only emits on an actual change, for as long as the session lives.
     */
    val syncState: StateFlow<SyncState>

    /**
     * Whether the client currently considers itself online, derived from [syncState].
     * This is `false` only while the state is [SyncState.Offline]; an idle or errored sync still counts as online.
     */
    val isOnline: StateFlow<Boolean>
}
