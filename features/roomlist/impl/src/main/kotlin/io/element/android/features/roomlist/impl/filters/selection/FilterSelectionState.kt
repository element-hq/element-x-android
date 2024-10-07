/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomlist.impl.filters.selection

import io.element.android.features.roomlist.impl.filters.RoomListFilter

data class FilterSelectionState(
    val filter: RoomListFilter,
    val isSelected: Boolean,
)
