/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

import io.element.android.libraries.matrix.api.core.EventId

sealed interface CreateTimelineParams {
    data class Focused(val focusedEventId: EventId) : CreateTimelineParams
    data object MediaOnly : CreateTimelineParams
    data class MediaOnlyFocused(val focusedEventId: EventId) : CreateTimelineParams
    data object PinnedOnly : CreateTimelineParams
}
