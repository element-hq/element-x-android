/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.threads

sealed interface ThreadListPaginationStatus {
    data class Idle(
        val hasMoreToLoad: Boolean,
    ) : ThreadListPaginationStatus

    data object Loading : ThreadListPaginationStatus
}
