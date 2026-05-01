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
 * Model if there is a new event in the timeline and if it is from me or from other.
 * This can be used to scroll to the bottom of the list when a new event is added.
 *
 * [FromOther] also carries the running count of incoming messages from other users since the
 * timeline was last at the bottom — used to drive the badge on the scroll-to-bottom button.
 */
@Immutable
sealed interface NewEventState {
    val messageCount: Int get() = 0

    data object None : NewEventState
    data object FromMe : NewEventState
    data class FromOther(override val messageCount: Int) : NewEventState
}
