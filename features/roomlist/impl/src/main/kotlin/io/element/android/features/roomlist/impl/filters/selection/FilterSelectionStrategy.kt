/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.roomlist.impl.filters.selection

import io.element.android.features.roomlist.impl.filters.RoomListFilter
import kotlinx.coroutines.flow.StateFlow

interface FilterSelectionStrategy {
    val filterSelectionStates: StateFlow<Set<FilterSelectionState>>

    fun select(filter: RoomListFilter)
    fun deselect(filter: RoomListFilter)
    fun isSelected(filter: RoomListFilter): Boolean
    fun clear()

    fun toggle(filter: RoomListFilter) {
        if (isSelected(filter)) {
            deselect(filter)
        } else {
            select(filter)
        }
    }
}
