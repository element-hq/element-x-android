/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.EventTimelineItem
import org.matrix.rustcomponents.sdk.NoHandle
import org.matrix.rustcomponents.sdk.TimelineItem
import org.matrix.rustcomponents.sdk.TimelineUniqueId
import org.matrix.rustcomponents.sdk.VirtualTimelineItem

class FakeFfiTimelineItem(
    private val asEventResult: EventTimelineItem? = null,
) : TimelineItem(NoHandle) {
    override fun asEvent(): EventTimelineItem? = asEventResult
    override fun asVirtual(): VirtualTimelineItem? = null
    override fun fmtDebug(): String = "fmtDebug"
    override fun uniqueId(): TimelineUniqueId = TimelineUniqueId("uniqueId")
}
