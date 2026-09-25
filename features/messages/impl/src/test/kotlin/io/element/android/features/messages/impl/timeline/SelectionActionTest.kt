/*
 * Copyright 2026 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.timeline.model.event.aStaticLocationMode
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemLocationContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemRedactedContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemStateEventContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemStickerContent
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import org.junit.Test

class SelectionActionTest {
    @Test
    fun `Forward - a remote text message can be selected`() {
        assertThat(SelectionAction.Forward.canApplyTo(aTimelineItemEvent())).isTrue()
    }

    @Test
    fun `Forward - a local echo cannot be selected`() {
        assertThat(SelectionAction.Forward.canApplyTo(aTimelineItemEvent().copy(eventId = null))).isFalse()
    }

    @Test
    fun `Forward - a failed to send message cannot be selected`() {
        // A message which failed to send is a local echo: it has no event id.
        val item = aTimelineItemEvent(sendState = LocalEventSendState.Failed.Unknown("failed")).copy(eventId = null)
        assertThat(SelectionAction.Forward.canApplyTo(item)).isFalse()
    }

    @Test
    fun `Forward - a state event cannot be selected`() {
        val item = aTimelineItemEvent(content = aTimelineItemStateEventContent())
        assertThat(SelectionAction.Forward.canApplyTo(item)).isFalse()
    }

    @Test
    fun `Forward - a redacted event cannot be selected`() {
        val item = aTimelineItemEvent(content = aTimelineItemRedactedContent())
        assertThat(SelectionAction.Forward.canApplyTo(item)).isFalse()
    }

    @Test
    fun `Forward - a sticker cannot be selected, it cannot be forwarded`() {
        val item = aTimelineItemEvent(content = aTimelineItemStickerContent())
        assertThat(SelectionAction.Forward.canApplyTo(item)).isFalse()
    }

    @Test
    fun `Forward - a static location can be selected, it can be forwarded`() {
        val item = aTimelineItemEvent(content = aTimelineItemLocationContent(mode = aStaticLocationMode()))
        assertThat(SelectionAction.Forward.canApplyTo(item)).isTrue()
    }
}
