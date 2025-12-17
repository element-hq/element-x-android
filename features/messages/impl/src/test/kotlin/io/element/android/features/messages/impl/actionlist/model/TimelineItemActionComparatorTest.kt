/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.actionlist.model

import org.junit.Test

class TimelineItemActionComparatorTest {
    @Test
    fun `check that the list in the comparator only contain each item once`() {
        val sut = TimelineItemActionComparator()
        sut.orderedList.forEach {
            require(sut.orderedList.count { item -> item == it } == 1, { "Duplicate ${it::class.java}.$it" })
        }
    }

    @Test
    fun `check that the list in the comparator contains all the items`() {
        val sut = TimelineItemActionComparator()
        TimelineItemAction.entries.forEach {
            require(it in sut.orderedList, { "Missing ${it::class.simpleName}.$it in orderedList" })
        }
    }
}
