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

package io.element.android.features.roomdetails.impl.members

import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.ui.room.sortingName
import java.text.Collator

// Comparator used to sort room members by power level (descending) and then by name (ascending)
internal class PowerLevelRoomMemberComparator : Comparator<RoomMember> {
    // Used to simplify and compare unicode and ASCII chars (á == a)
    private val collator = Collator.getInstance().apply {
        decomposition = Collator.CANONICAL_DECOMPOSITION
    }
    override fun compare(o1: RoomMember, o2: RoomMember): Int {
        return when {
            o1.powerLevel > o2.powerLevel -> return -1
            o1.powerLevel < o2.powerLevel -> return 1
            else -> {
                collator.compare(o1.sortingName(), o2.sortingName())
            }
        }
    }
}
