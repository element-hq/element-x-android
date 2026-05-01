/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model

import androidx.compose.runtime.Immutable

/**
 * Drives the jump-to-unread FAB and the count badge on the scroll-to-bottom FAB.
 *
 * The two affordances share state because they're both gated on the same feature flag and both
 * use counts derived from the same timeline scan.
 */
@Immutable
sealed interface JumpToUnreadState {
    /** Feature flag is off — neither the FAB nor the new-message badge is shown. */
    data object Disabled : JumpToUnreadState

    /** Feature flag is on, but no read marker is present in the current timeline window. */
    data object NoMarker : JumpToUnreadState

    /**
     * Feature flag is on and the read marker is loaded at [markerIndex]. The FAB shows when the
     * marker is above the viewport; the badge displays [unreadCount] when greater than zero.
     */
    data class Loaded(val markerIndex: Int, val unreadCount: Int) : JumpToUnreadState
}
