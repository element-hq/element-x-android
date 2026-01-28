/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.search

sealed interface RoomListSearchEvent {
    data object ToggleSearchVisibility : RoomListSearchEvent
    data object ClearQuery : RoomListSearchEvent
    data class UpdateVisibleRange(val range: IntRange) : RoomListSearchEvent
}
