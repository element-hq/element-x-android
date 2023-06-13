/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages

import android.net.Uri
import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.fixtures.aMessageEvent
import io.element.android.features.messages.fixtures.aTimelineItemsFactory
import io.element.android.features.messages.impl.MessagesEvents
import io.element.android.features.messages.impl.MessagesPresenter
import io.element.android.features.messages.impl.actionlist.ActionListPresenter
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.messagecomposer.MessageComposerPresenter
import io.element.android.features.messages.impl.timeline.TimelinePresenter
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.media.FakeLocalMediaFactory
import io.element.android.features.messages.utils.messagesummary.FakeMessageSummaryFormatter
import io.element.android.features.networkmonitor.test.FakeNetworkMonitor
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.designsystem.utils.SnackbarDispatcher
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.room.aFakeMatrixRoom
import io.element.android.libraries.mediapickers.test.FakePickerProvider
import io.element.android.libraries.mediaupload.api.MediaSender
import io.element.android.libraries.mediaupload.test.FakeMediaPreProcessor
import io.element.android.libraries.textcomposer.MessageComposerMode
import io.element.android.tests.testutils.testCoroutineDispatchers
import io.mockk.mockk
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MessagesPresenterTest {

    private val mockMediaUrl: Uri = mockk("localMediaUri")

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createMessagePresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.roomId).isEqualTo(A_ROOM_ID)
        }
    }

    @Test
    fun `present - handle sending a reaction`() = runTest {
        val room = aFakeMatrixRoom()
        val presenter = createMessagePresenter(matrixRoom = room)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(MessagesEvents.SendReaction("👍", AN_EVENT_ID))
            assertThat(room.sendReactionCount).isEqualTo(1)

            // No crashes when sending a reaction failed
            room.givenSendReactionResult(Result.failure(IllegalStateException("Failed to send reaction")))
            initialState.eventSink.invoke(MessagesEvents.SendReaction("👍", AN_EVENT_ID))
            assertThat(room.sendReactionCount).isEqualTo(2)
        }
    }

    @Test
    fun `present - handle action forward`() = runTest {
        val presenter = createMessagePresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Forward, aMessageEvent()))
            // Still a TODO in the code
        }
    }

    @Test
    fun `present - handle action copy`() = runTest {
        val presenter = createMessagePresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Copy, aMessageEvent()))
            // Still a TODO in the code
        }
    }

    @Test
    fun `present - handle action reply`() = runTest {
        val presenter = createMessagePresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Reply, aMessageEvent()))
            skipItems(1)
            val finalState = awaitItem()
            assertThat(finalState.composerState.mode).isInstanceOf(MessageComposerMode.Reply::class.java)
        }
    }

    @Test
    fun `present - handle action reply to an event with no id does nothing`() = runTest {
        val presenter = createMessagePresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Reply, aMessageEvent(eventId = null)))
            skipItems(1)
            // Otherwise we would have some extra items here
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - handle action reply to an image media message`() = runTest {
        val presenter = createMessagePresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            val mediaMessage = aMessageEvent(
                content = TimelineItemImageContent(
                    body = "image.jpg",
                    mediaSource = MediaSource(AN_AVATAR_URL),
                    mimeType = MimeTypes.Jpeg,
                    blurhash = null,
                    width = 20,
                    height = 20,
                    aspectRatio = 1.0f,
                    fileExtension = "jpg",
                    formattedFileSize = "4MB"
                )
            )
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Reply, mediaMessage))
            skipItems(1)
            val finalState = awaitItem()
            assertThat(finalState.composerState.mode).isInstanceOf(MessageComposerMode.Reply::class.java)
            val replyMode = finalState.composerState.mode as MessageComposerMode.Reply
            assertThat(replyMode.attachmentThumbnailInfo).isNotNull()
        }
    }

    @Test
    fun `present - handle action reply to a video media message`() = runTest {
        val presenter = createMessagePresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            val mediaMessage = aMessageEvent(
                content = TimelineItemVideoContent(
                    body = "video.mp4",
                    duration = 10L,
                    videoSource = MediaSource(AN_AVATAR_URL),
                    thumbnailSource = MediaSource(AN_AVATAR_URL),
                    mimeType = MimeTypes.Mp4,
                    blurHash = null,
                    width = 20,
                    height = 20,
                    aspectRatio = 1.0f,
                    fileExtension = "mp4",
                    formattedFileSize = "50MB"
                )
            )
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Reply, mediaMessage))
            skipItems(1)
            val finalState = awaitItem()
            assertThat(finalState.composerState.mode).isInstanceOf(MessageComposerMode.Reply::class.java)
            val replyMode = finalState.composerState.mode as MessageComposerMode.Reply
            assertThat(replyMode.attachmentThumbnailInfo).isNotNull()
        }
    }

    @Test
    fun `present - handle action reply to a file media message`() = runTest {
        val presenter = createMessagePresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            val mediaMessage = aMessageEvent(
                content = TimelineItemFileContent(
                    body = "file.pdf",
                    fileSource = MediaSource(AN_AVATAR_URL),
                    thumbnailSource = MediaSource(AN_AVATAR_URL),
                    formattedFileSize = "10 MB",
                    mimeType = MimeTypes.Pdf,
                    fileExtension = "pdf",
                )
            )
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Reply, mediaMessage))
            skipItems(1)
            val finalState = awaitItem()
            assertThat(finalState.composerState.mode).isInstanceOf(MessageComposerMode.Reply::class.java)
            val replyMode = finalState.composerState.mode as MessageComposerMode.Reply
            assertThat(replyMode.attachmentThumbnailInfo).isNotNull()
        }
    }

    @Test
    fun `present - handle action edit`() = runTest {
        val presenter = createMessagePresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Edit, aMessageEvent()))
            skipItems(1)
            val finalState = awaitItem()
            assertThat(finalState.composerState.mode).isInstanceOf(MessageComposerMode.Edit::class.java)
        }
    }

    @Test
    fun `present - handle action redact`() = runTest {
        val matrixRoom = aFakeMatrixRoom()
        val presenter = createMessagePresenter(matrixRoom)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Redact, aMessageEvent()))
            assertThat(matrixRoom.redactEventEventIdParam).isEqualTo(AN_EVENT_ID)
        }
    }

    @Test
    fun `present - handle action report content`() = runTest {
        val presenter = createMessagePresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.ReportContent, aMessageEvent()))
            // Still a TODO in the code
        }
    }

    @Test
    fun `present - handle action show developer info`() = runTest {
        val presenter = createMessagePresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Developer, aMessageEvent()))
            // Still a TODO in the code
        }
    }

    private fun TestScope.createMessagePresenter(
        matrixRoom: MatrixRoom = aFakeMatrixRoom()
    ): MessagesPresenter {
        val messageComposerPresenter = MessageComposerPresenter(
            appCoroutineScope = this,
            room = matrixRoom,
            mediaPickerProvider = FakePickerProvider(),
            featureFlagService = FakeFeatureFlagService(),
            localMediaFactory = FakeLocalMediaFactory(mockMediaUrl),
            mediaSender = MediaSender(FakeMediaPreProcessor(), matrixRoom),
            snackbarDispatcher = SnackbarDispatcher(),
        )
        val timelinePresenter = TimelinePresenter(
            timelineItemsFactory = aTimelineItemsFactory(),
            room = matrixRoom,
        )
        val buildMeta = BuildMeta(
            buildType = BuildType.DEBUG,
            isDebuggable = true,
            applicationId = "",
            applicationName = "",
            lowPrivacyLoggingEnabled = true,
            versionName = "",
            gitRevision = "",
            gitBranchName = "",
            gitRevisionDate = "",
            flavorDescription = "",
            flavorShortDescription = "",
        )
        val actionListPresenter = ActionListPresenter(buildMeta = buildMeta)
        return MessagesPresenter(
            room = matrixRoom,
            composerPresenter = messageComposerPresenter,
            timelinePresenter = timelinePresenter,
            actionListPresenter = actionListPresenter,
            networkMonitor = FakeNetworkMonitor(),
            snackbarDispatcher = SnackbarDispatcher(),
            messageSummaryFormatter = FakeMessageSummaryFormatter(),
            dispatchers = testCoroutineDispatchers(),
        )
    }
}
