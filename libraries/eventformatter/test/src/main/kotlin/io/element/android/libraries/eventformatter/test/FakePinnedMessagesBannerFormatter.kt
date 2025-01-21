/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.eventformatter.test

import io.element.android.libraries.eventformatter.api.PinnedMessagesBannerFormatter
import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem

class FakePinnedMessagesBannerFormatter(
    val formatLambda: (event: EventTimelineItem) -> CharSequence
) : PinnedMessagesBannerFormatter {
    override fun format(event: EventTimelineItem): CharSequence {
        return formatLambda(event)
    }
}
