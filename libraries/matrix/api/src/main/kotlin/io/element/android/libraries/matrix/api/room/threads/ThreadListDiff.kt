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

suspend fun <T> MutableList<T>.applyDiffs(
    diffs: List<ThreadListDiff>,
    transform: suspend (ThreadListItem) -> T,
) {
    for (diff in diffs) {
        when (diff) {
            is ThreadListDiff.Reset -> {
                clear()
                for (value in diff.values) {
                    add(transform(value))
                }
            }
            is ThreadListDiff.Append -> {
                for (value in diff.values) {
                    add(transform(value))
                }
            }
            is ThreadListDiff.Clear -> clear()
            is ThreadListDiff.Insert -> add(diff.index, transform(diff.value))
            is ThreadListDiff.Set -> set(diff.index, transform(diff.value))
            is ThreadListDiff.Remove -> removeAt(diff.index)
            is ThreadListDiff.PushBack -> add(transform(diff.value))
            is ThreadListDiff.PushFront -> add(0, transform(diff.value))
            is ThreadListDiff.PopBack -> if (isNotEmpty()) removeAt(lastIndex)
            is ThreadListDiff.PopFront -> if (isNotEmpty()) removeAt(0)
            is ThreadListDiff.Truncate -> if (diff.length < size) subList(diff.length, size).clear()
        }
    }
}
