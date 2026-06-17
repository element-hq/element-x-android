/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.recentcalls.impl.recentcalls

import io.element.android.features.recentcalls.api.RecentCallEntry
import io.element.android.features.recentcalls.api.RecentCallsFilter

sealed interface RecentCallsEvent {
    data class SelectFilter(val filter: RecentCallsFilter) : RecentCallsEvent
    data object LoadMore : RecentCallsEvent
    data class CallBack(val entry: RecentCallEntry) : RecentCallsEvent
}
