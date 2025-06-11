/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.InsertData
import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.SetData
import org.matrix.rustcomponents.sdk.TimelineChange
import org.matrix.rustcomponents.sdk.TimelineDiff
import org.matrix.rustcomponents.sdk.TimelineItem

class FakeFfiTimelineDiff(
    private val change: TimelineChange,
    private val item: TimelineItem? = FakeFfiTimelineItem()
) : TimelineDiff(NoPointer) {
    override fun change() = change
    override fun append(): List<TimelineItem>? = item?.let { listOf(it) }
    override fun insert(): InsertData? = item?.let { InsertData(1u, it) }
    override fun pushBack(): TimelineItem? = item
    override fun pushFront(): TimelineItem? = item
    override fun remove(): UInt? = 1u
    override fun reset(): List<TimelineItem>? = item?.let { listOf(it) }
    override fun set(): SetData? = item?.let { SetData(1u, it) }
    override fun truncate(): UInt? = 1u
}
