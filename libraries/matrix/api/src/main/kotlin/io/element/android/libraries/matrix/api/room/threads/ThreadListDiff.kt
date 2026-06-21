/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.threads

sealed interface ThreadListDiff {
    data class Append(val values: List<ThreadListItem>) : ThreadListDiff
    data object Clear : ThreadListDiff
    data class PushFront(val value: ThreadListItem) : ThreadListDiff
    data class PushBack(val value: ThreadListItem) : ThreadListDiff
    data object PopFront : ThreadListDiff
    data object PopBack : ThreadListDiff
    data class Insert(val index: Int, val value: ThreadListItem) : ThreadListDiff
    data class Set(val index: Int, val value: ThreadListItem) : ThreadListDiff
    data class Remove(val index: Int) : ThreadListDiff
    data class Truncate(val length: Int) : ThreadListDiff
    data class Reset(val values: List<ThreadListItem>) : ThreadListDiff
}
