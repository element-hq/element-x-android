/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.eventformatter.test

import io.element.android.libraries.eventformatter.api.RoomLastMessageFormatter
import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem

class FakeRoomLastMessageFormatter : RoomLastMessageFormatter {
    private var result: CharSequence? = null

    override fun format(event: EventTimelineItem, isDmRoom: Boolean): CharSequence? {
        return result
    }

    fun givenFormatResult(result: CharSequence?) {
        this.result = result
    }
}
