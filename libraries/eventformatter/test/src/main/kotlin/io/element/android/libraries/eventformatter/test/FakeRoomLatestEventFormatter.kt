/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.eventformatter.test

import io.element.android.libraries.eventformatter.api.RoomLatestEventFormatter
import io.element.android.libraries.matrix.api.roomlist.LatestEventValue

class FakeRoomLatestEventFormatter : RoomLatestEventFormatter {
    private var result: CharSequence? = null

    override fun format(latestEvent: LatestEventValue, isDmRoom: Boolean): CharSequence? {
        return result
    }

    fun givenFormatResult(result: CharSequence?) {
        this.result = result
    }
}
