/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.services.appnavstate.api

import kotlinx.coroutines.flow.StateFlow

/**
 * A service that tracks the foreground state of the app.
 */
interface AppForegroundStateService {
    /**
     * Any updates to the foreground state of the app will be emitted here.
     */
    val isInForeground: StateFlow<Boolean>

    /**
     * Start observing the foreground state.
     */
    fun start()
}
