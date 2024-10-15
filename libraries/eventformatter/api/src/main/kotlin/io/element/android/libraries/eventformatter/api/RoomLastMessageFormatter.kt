/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.eventformatter.api

import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem

fun interface RoomLastMessageFormatter {
    fun format(event: EventTimelineItem, isDmRoom: Boolean): CharSequence?
}
