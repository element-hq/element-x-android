/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.pinned.list

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import org.junit.Test

class PinnedMessagesListTimelineActionPostProcessorTest {
    @Test
    fun `ensure that ViewInTimeline is added`() {
        val sut = PinnedMessagesListTimelineActionPostProcessor()
        val result = sut.process(
            listOf()
        )
        assertThat(result).isEqualTo(
            listOf(TimelineItemAction.ViewInTimeline)
        )
    }

    @Test
    fun `ensure that some actions are kept and some other are filtered out`() {
        val sut = PinnedMessagesListTimelineActionPostProcessor()
        val result = sut.process(
            listOf(
                TimelineItemAction.Forward,
                TimelineItemAction.CopyText,
                TimelineItemAction.CopyCaption,
                TimelineItemAction.CopyLink,
                TimelineItemAction.Redact,
                TimelineItemAction.Reply,
                TimelineItemAction.ReplyInThread,
                TimelineItemAction.Edit,
                TimelineItemAction.EditCaption,
                TimelineItemAction.AddCaption,
                TimelineItemAction.RemoveCaption,
                TimelineItemAction.ViewSource,
                TimelineItemAction.ReportContent,
                TimelineItemAction.EndPoll,
                TimelineItemAction.Pin,
                TimelineItemAction.Unpin,
            )
        )
        assertThat(result).isEqualTo(
            listOf(
                TimelineItemAction.ViewInTimeline,
                TimelineItemAction.Unpin,
                TimelineItemAction.Forward,
                TimelineItemAction.ViewSource,
            )
        )
    }
}
