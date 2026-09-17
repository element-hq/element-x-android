/*
 * Copyright 2026 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.EventId
import kotlinx.collections.immutable.ImmutableSet

/** Maximum number of messages that can be selected at once. */
const val MAX_SELECTION_COUNT = 30

@Immutable
sealed interface SelectionState {
    /** Selection mode is not active. */
    data object Disabled : SelectionState

    /** Selection mode is active with the given selection. */
    data class Active(val selectedEventIds: ImmutableSet<EventId>) : SelectionState
}
