/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.search

import androidx.compose.runtime.Immutable

@Immutable
sealed interface GlobalSearchEvent {
    object ClearQuery : GlobalSearchEvent
    object ToggleSearchVisibility : GlobalSearchEvent
    data class UpdateVisibleRange(val range: IntRange) : GlobalSearchEvent
    data class UpdateTarget(val target: GlobalSearchTarget) : GlobalSearchEvent
}
