/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.ftue.api.state

import kotlinx.coroutines.flow.StateFlow

/**
 * Service to manage the First Time User Experience state (aka Onboarding).
 */
interface FtueService {
    /** The current state of the FTUE. */
    val state: StateFlow<FtueState>

    /** Reset the FTUE state. */
    suspend fun reset()
}

/** The state of the FTUE. */
sealed interface FtueState {
    /** The FTUE state is unknown, nothing to do for now. */
    data object Unknown : FtueState

    /** The FTUE state is incomplete. The FTUE flow should be displayed. */
    data object Incomplete : FtueState

    /** The FTUE state is complete. The FTUE flow should not be displayed anymore. */
    data object Complete : FtueState
}
