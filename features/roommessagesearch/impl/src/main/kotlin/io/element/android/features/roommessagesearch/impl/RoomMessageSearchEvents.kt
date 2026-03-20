/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roommessagesearch.impl

sealed interface RoomMessageSearchEvents {
    data class UpdateQuery(val query: String) : RoomMessageSearchEvents
    data object LoadMore : RoomMessageSearchEvents
    data object RetrySearch : RoomMessageSearchEvents
}
