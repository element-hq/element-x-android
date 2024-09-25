/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.EventTimelineItem
import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.TimelineItem
import org.matrix.rustcomponents.sdk.VirtualTimelineItem

class FakeRustTimelineItem : TimelineItem(NoPointer) {
    override fun asEvent(): EventTimelineItem? = null
    override fun asVirtual(): VirtualTimelineItem? = null
    override fun fmtDebug(): String = "fmtDebug"
    override fun uniqueId(): String = "uniqueId"
}
