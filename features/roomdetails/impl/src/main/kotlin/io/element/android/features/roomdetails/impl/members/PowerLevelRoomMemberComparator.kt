/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
