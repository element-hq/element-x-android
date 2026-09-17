/*
 * Copyright 2026 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemRedactedContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemStateEventContent
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import org.junit.Test

class TimelineItemSelectionTest {
    @Test
    fun `a remote text message can be selected`() {
        val item = aTimelineItemEvent()
        assertThat(item.canBeSelected()).isTrue()
    }

    @Test
    fun `a local echo cannot be selected`() {
        val item = aTimelineItemEvent().copy(eventId = null)
        assertThat(item.canBeSelected()).isFalse()
    }

    @Test
    fun `a failed to send message cannot be selected`() {
        val item = aTimelineItemEvent(sendState = LocalEventSendState.Failed.Unknown("failed"))
        assertThat(item.canBeSelected()).isFalse()
    }

    @Test
    fun `a state event cannot be selected`() {
        val item = aTimelineItemEvent(content = aTimelineItemStateEventContent())
        assertThat(item.canBeSelected()).isFalse()
    }

    @Test
    fun `a redacted event cannot be selected`() {
        val item = aTimelineItemEvent(content = aTimelineItemRedactedContent())
        assertThat(item.canBeSelected()).isFalse()
    }
}
