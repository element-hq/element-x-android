/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.notification.CallIntent
import org.junit.Test

class TimelineItemEventContentSelectionTest {
    @Test
    fun `isBulkSelectable is false for noise - state changes, redacted, call notifications, unknown`() {
        assertThat(aTimelineItemStateEventContent().isBulkSelectable()).isFalse()
        assertThat(TimelineItemRedactedContent.isBulkSelectable()).isFalse()
        assertThat(TimelineItemLegacyCallInviteContent.isBulkSelectable()).isFalse()
        assertThat(
            TimelineItemRtcNotificationContent(CallIntent.AUDIO, RtcNotificationState.Started).isBulkSelectable()
        ).isFalse()
        assertThat(TimelineItemUnknownContent.isBulkSelectable()).isFalse()
    }

    @Test
    fun `isBulkSelectable is true for real messages`() {
        listOf(
            aTimelineItemTextContent(),
            aTimelineItemNoticeContent(),
            aTimelineItemImageContent(),
            aTimelineItemVideoContent(),
            aTimelineItemFileContent(),
            aTimelineItemAudioContent(),
            aTimelineItemVoiceContent(),
            aTimelineItemPollContent(),
            aTimelineItemStickerContent(),
        ).forEach { assertThat(it.isBulkSelectable()).isTrue() }
    }

    @Test
    fun `opensMediaViewer is true only for content that has its own viewer`() {
        listOf(
            aTimelineItemImageContent(),
            aTimelineItemVideoContent(),
            aTimelineItemFileContent(),
            aTimelineItemAudioContent(),
            aTimelineItemLocationContent(mode = aStaticLocationMode()),
        ).forEach { assertThat(it.opensMediaViewer()).isTrue() }
    }

    @Test
    fun `opensMediaViewer is false for text-like and non-viewer content`() {
        listOf(
            aTimelineItemTextContent(),
            aTimelineItemPollContent(),
            aTimelineItemStickerContent(),
            aTimelineItemVoiceContent(),
            aTimelineItemStateEventContent(),
            TimelineItemRedactedContent,
        ).forEach { assertThat(it.opensMediaViewer()).isFalse() }
    }
}
