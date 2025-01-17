/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.appnavstate.api

/**
 * Observes the app state and network state to start/stop the sync service.
 */
interface SyncOrchestrator {
    fun start()
    fun stop()
}
