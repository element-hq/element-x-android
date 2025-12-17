/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.protection

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.media.MediaPreviewConfig
import io.element.android.libraries.matrix.api.media.MediaPreviewService
import io.element.android.libraries.matrix.api.media.MediaPreviewValue
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.media.FakeMediaPreviewService
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class TimelineProtectionPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.protectionState).isEqualTo(ProtectionState.RenderAll)
        }
    }

    @Test
    fun `present - media preview value off`() = runTest {
        val mediaPreviewConfig = MediaPreviewConfig(mediaPreviewValue = MediaPreviewValue.Off, hideInviteAvatar = false)
        val mediaPreviewService = FakeMediaPreviewService(mediaPreviewConfigFlow = MutableStateFlow(mediaPreviewConfig))
        val presenter = createPresenter(mediaPreviewService = mediaPreviewService)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.protectionState).isEqualTo(ProtectionState.RenderOnly(persistentSetOf()))
            // ShowContent with null should have no effect.
            initialState.eventSink(TimelineProtectionEvent.ShowContent(eventId = null))
            initialState.eventSink(TimelineProtectionEvent.ShowContent(eventId = AN_EVENT_ID))
            val finalState = awaitItem()
            assertThat(finalState.protectionState).isEqualTo(ProtectionState.RenderOnly(persistentSetOf(AN_EVENT_ID)))
        }
    }

    @Test
    fun `present - media preview value private in public room`() = runTest {
        val mediaPreviewConfig = MediaPreviewConfig(mediaPreviewValue = MediaPreviewValue.Private, hideInviteAvatar = false)
        val mediaPreviewService = FakeMediaPreviewService(mediaPreviewConfigFlow = MutableStateFlow(mediaPreviewConfig))
        val room = FakeBaseRoom(initialRoomInfo = aRoomInfo(joinRule = JoinRule.Public))
        val presenter = createPresenter(mediaPreviewService = mediaPreviewService, room = room)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.protectionState).isEqualTo(ProtectionState.RenderOnly(persistentSetOf()))
            // ShowContent with null should have no effect.
            initialState.eventSink(TimelineProtectionEvent.ShowContent(eventId = null))
            initialState.eventSink(TimelineProtectionEvent.ShowContent(eventId = AN_EVENT_ID))
            val finalState = awaitItem()
            assertThat(finalState.protectionState).isEqualTo(ProtectionState.RenderOnly(persistentSetOf(AN_EVENT_ID)))
        }
    }

    @Test
    fun `present - media preview value private in non public room`() = runTest {
        val mediaPreviewConfig = MediaPreviewConfig(mediaPreviewValue = MediaPreviewValue.Private, hideInviteAvatar = false)
        val mediaPreviewService = FakeMediaPreviewService(mediaPreviewConfigFlow = MutableStateFlow(mediaPreviewConfig))
        val room = FakeBaseRoom(initialRoomInfo = aRoomInfo(joinRule = JoinRule.Invite))
        val presenter = createPresenter(mediaPreviewService = mediaPreviewService, room = room)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.protectionState).isEqualTo(ProtectionState.RenderAll)
            // ShowContent with null should have no effect.
            initialState.eventSink(TimelineProtectionEvent.ShowContent(eventId = null))
            initialState.eventSink(TimelineProtectionEvent.ShowContent(eventId = AN_EVENT_ID))
        }
    }

    private fun createPresenter(
        room: BaseRoom = FakeBaseRoom(),
        mediaPreviewService: MediaPreviewService = FakeMediaPreviewService(),
    ) = TimelineProtectionPresenter(
        mediaPreviewService = mediaPreviewService,
        room = room,
    )
}
