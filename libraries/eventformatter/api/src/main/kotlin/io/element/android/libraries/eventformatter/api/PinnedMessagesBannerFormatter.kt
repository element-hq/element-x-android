/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.eventformatter.api

import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem

/**
 * Formats the short summary of a pinned event shown in the banner at the top of a room.
 */
interface PinnedMessagesBannerFormatter {
    /**
     * @param event the pinned event to describe.
     */
    fun format(event: EventTimelineItem): CharSequence
}
