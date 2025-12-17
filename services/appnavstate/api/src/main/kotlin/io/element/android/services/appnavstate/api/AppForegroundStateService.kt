/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
     * Updates to whether the app is active because an incoming ringing call is happening will be emitted here.
     */
    val hasRingingCall: StateFlow<Boolean>

    /**
     * Updates to whether the app is in an active call or not will be emitted here.
     */
    val isInCall: StateFlow<Boolean>

    /**
     * Updates to whether the app is syncing a notification event or not will be emitted here.
     */
    val isSyncingNotificationEvent: StateFlow<Boolean>

    /**
     * Start observing the foreground state.
     */
    fun startObservingForeground()

    /**
     * Update the in-call state.
     */
    fun updateIsInCallState(isInCall: Boolean)

    /**
     * Update the 'has ringing call' state.
     */
    fun updateHasRingingCall(hasRingingCall: Boolean)

    /**
     * Update the active state for the syncing notification event flow.
     */
    fun updateIsSyncingNotificationEvent(isSyncingNotificationEvent: Boolean)
}
