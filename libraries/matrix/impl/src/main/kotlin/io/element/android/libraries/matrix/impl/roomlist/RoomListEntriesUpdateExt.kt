/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.matrix.impl.roomlist

import org.matrix.rustcomponents.sdk.RoomListEntriesUpdate

internal fun RoomListEntriesUpdate.describe(): String {
    return when (this) {
        is RoomListEntriesUpdate.Set -> {
            "Set #$index to '${value.displayName()}'"
        }
        is RoomListEntriesUpdate.Append -> {
            "Append ${values.map { "'" + it.displayName() + "'" }}"
        }
        is RoomListEntriesUpdate.PushBack -> {
            "PushBack '${value.displayName()}'"
        }
        is RoomListEntriesUpdate.PushFront -> {
            "PushFront '${value.displayName()}'"
        }
        is RoomListEntriesUpdate.Insert -> {
            "Insert at #$index: '${value.displayName()}'"
        }
        is RoomListEntriesUpdate.Remove -> {
            "Remove #$index"
        }
        is RoomListEntriesUpdate.Reset -> {
            "Reset all to ${values.map { "'" + it.displayName() + "'" }}"
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
