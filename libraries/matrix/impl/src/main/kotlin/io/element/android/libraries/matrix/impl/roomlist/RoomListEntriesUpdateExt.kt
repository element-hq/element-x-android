/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomlist

import io.element.android.libraries.architecture.coverage.ExcludeFromCoverage
import org.matrix.rustcomponents.sdk.RoomListEntriesUpdate

@Suppress("unused")
@ExcludeFromCoverage
internal fun RoomListEntriesUpdate.describe(): String {
    return when (this) {
        is RoomListEntriesUpdate.Set -> {
            "Set #$index to '${value.id()}'"
        }
        is RoomListEntriesUpdate.Append -> {
            "Append ${values.map { "'" + it.id() + "'" }}"
        }
        is RoomListEntriesUpdate.PushBack -> {
            "PushBack '${value.id()}'"
        }
        is RoomListEntriesUpdate.PushFront -> {
            "PushFront '${value.id()}'"
        }
        is RoomListEntriesUpdate.Insert -> {
            "Insert at #$index: '${value.id()}'"
        }
        is RoomListEntriesUpdate.Remove -> {
            "Remove #$index"
        }
        is RoomListEntriesUpdate.Reset -> {
            "Reset all to ${values.map { "'" + it.id() + "'" }}"
        }
        RoomListEntriesUpdate.PopBack -> {
            "PopBack"
        }
        RoomListEntriesUpdate.PopFront -> {
            "PopFront"
        }
        RoomListEntriesUpdate.Clear -> {
            "Clear"
        }
        is RoomListEntriesUpdate.Truncate -> {
            "Truncate to $length items"
        }
    }
}
