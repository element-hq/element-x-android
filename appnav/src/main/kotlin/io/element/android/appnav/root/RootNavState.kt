/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.root

import io.element.android.libraries.sessionstorage.api.LoggedInState

/**
 * [RootNavState] produced by [RootNavStateFlowFactory].
 */
data class RootNavState(
    /**
     * This value is incremented when a clear cache is done.
     * Can be useful to track to force ui state to re-render
     */
    val cacheIndex: Int,
    /**
     * LoggedInState.
     */
    val loggedInState: LoggedInState,
)
