/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.protection

import com.google.common.truth.Truth.assertThat
import io.element.android.features.contentscanner.api.ContentScannerService
import io.element.android.libraries.matrix.api.media.MediaPreviewConfig
import io.element.android.libraries.matrix.api.media.MediaPreviewService
import io.element.android.libraries.matrix.api.media.MediaPreviewValue
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.media.FakeMediaPreviewService
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.matrix.ui.media.contentvalidation.ContentValidationState
import io.element.android.libraries.matrix.ui.media.contentvalidation.ContentValidationValue
import io.element.android.libraries.matrix.ui.media.contentvalidation.DefaultContentValidationState
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.test
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
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

    @Test
    fun `present - shown content is restored when the presenter is recreated`() = runTest {
        val mediaPreviewConfig = MediaPreviewConfig(mediaPreviewValue = MediaPreviewValue.Off, hideInviteAvatar = false)
        val mediaPreviewService = FakeMediaPreviewService(mediaPreviewConfigFlow = MutableStateFlow(mediaPreviewConfig))
        val timelineProtectionStore = DefaultTimelineProtectionStore()
        createPresenter(mediaPreviewService = mediaPreviewService, timelineProtectionStore = timelineProtectionStore).test {
            val initialState = awaitItem()
            assertThat(initialState.protectionState).isEqualTo(ProtectionState.RenderOnly(persistentSetOf()))
            initialState.eventSink(TimelineProtectionEvent.ShowContent(eventId = AN_EVENT_ID))
            val finalState = awaitItem()
            assertThat(finalState.protectionState).isEqualTo(ProtectionState.RenderOnly(persistentSetOf(AN_EVENT_ID)))
        }
        createPresenter(mediaPreviewService = mediaPreviewService, timelineProtectionStore = timelineProtectionStore).test {
            val initialState = awaitItem()
            assertThat(initialState.protectionState).isEqualTo(ProtectionState.RenderOnly(persistentSetOf(AN_EVENT_ID)))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `present - validate media scans the media source`() = runTest {
        val url = "https://example.com/media"

        val mediaPreviewConfig = MediaPreviewConfig(mediaPreviewValue = MediaPreviewValue.Private, hideInviteAvatar = false)
        val mediaPreviewService = FakeMediaPreviewService(mediaPreviewConfigFlow = MutableStateFlow(mediaPreviewConfig))
        val room = FakeBaseRoom(initialRoomInfo = aRoomInfo(joinRule = JoinRule.Invite), roomCoroutineScope = backgroundScope)
        val contentScannerService = lambdaRecorder { _: List<MediaSource>, state: ContentValidationState ->
            state.update(url, ContentValidationValue.Valid)
        }
        val presenter = createPresenter(
            mediaPreviewService = mediaPreviewService,
            room = room,
            contentScannerService = contentScannerService,
        )
        presenter.test {
            val validationState = DefaultContentValidationState(initial = mapOf(url to ContentValidationValue.Unknown))
            assertThat(validationState.getCurrentOverallState().isValid()).isFalse()

            val initialState = awaitItem()
            initialState.eventSink(
                TimelineProtectionEvent.ValidateContent(
                    mediaSources = listOf(MediaSource(url)),
                    validationState = validationState
                )
            )

            runCurrent()

            contentScannerService.assertions().isCalledOnce()
            assertThat(validationState.getCurrentOverallState().isValid()).isTrue()
        }
    }

    private fun createPresenter(
        room: BaseRoom = FakeBaseRoom(),
        mediaPreviewService: MediaPreviewService = FakeMediaPreviewService(),
        contentScannerService: ContentScannerService = ContentScannerService { _: List<MediaSource>, _: ContentValidationState -> },
        timelineProtectionStore: TimelineProtectionStore = DefaultTimelineProtectionStore(),
    ) = TimelineProtectionPresenter(
        mediaPreviewService = mediaPreviewService,
        room = room,
        contentScannerService = contentScannerService,
        timelineProtectionStore = timelineProtectionStore,
    )
}
