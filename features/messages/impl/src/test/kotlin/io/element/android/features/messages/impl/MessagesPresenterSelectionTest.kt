/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.messages.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.features.location.test.FakeActiveLiveLocationShareManager
import io.element.android.features.messages.impl.actionlist.ActionListEvent
import io.element.android.features.messages.impl.actionlist.anActionListState
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.crypto.identity.anIdentityChangeState
import io.element.android.features.messages.impl.link.aLinkState
import io.element.android.features.messages.impl.messagecomposer.MessageComposerState
import io.element.android.features.messages.impl.messagecomposer.aMessageComposerState
import io.element.android.features.messages.impl.pinned.banner.aLoadedPinnedMessagesBannerState
import io.element.android.features.messages.impl.selection.FakeSelectionMediaSaver
import io.element.android.features.messages.impl.selection.TimelineSelectionState
import io.element.android.features.messages.impl.timeline.FakeMarkAsFullyRead
import io.element.android.features.messages.impl.timeline.MarkAsFullyRead
import io.element.android.features.messages.impl.timeline.TimelineController
import io.element.android.features.messages.impl.timeline.TimelineEvent
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.aTimelineState
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.features.messages.impl.timeline.protection.aTimelineProtectionState
import io.element.android.features.messages.test.timeline.FakeHtmlConverterProvider
import io.element.android.features.messages.test.timeline.voicemessages.composer.FakeDefaultVoiceMessageComposerPresenterFactory
import io.element.android.features.roomcall.api.aStandByCallState
import io.element.android.features.roommembermoderation.api.RoomMemberModerationState
import io.element.android.libraries.androidutils.clipboard.FakeClipboardHelper
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.emoji.api.recentemojis.AddRecentEmoji
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import io.element.android.libraries.matrix.api.timeline.item.event.toEventOrTransactionId
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.matrix.test.room.powerlevels.FakeRoomPermissions
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.textcomposer.model.aTextEditorStateMarkdown
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.consumeItemsUntilTimeout
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.testCoroutineDispatchers
import io.element.android.tests.testutils.testWithLifecycleOwner
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class MessagesPresenterSelectionTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `ToggleSelection stops adding once the cap is reached`() = runTest {
        val cap = TimelineSelectionState.MAX_SELECTION
        val events = (0 until cap + 5).map { aTimelineItemEvent(eventId = EventId("\$E-$it")) }
        val presenter = createMessagesPresenter(
            timelineItems = events.toImmutableList(),
        )
        presenter.testWithLifecycleOwner {
            val initial = awaitItem()
            events.forEach { initial.eventSink(MessagesEvent.ToggleSelection(it)) }
            val state = consumeItemsUntilPredicate { it.selectionState.isAtCap }.last()
            assertThat(state.selectionState.count).isEqualTo(cap)
            assertThat(state.selectionState.isAtCap).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ToggleSelection ignores events that are not bulk-selectable`() = runTest {
        val m1 = aTimelineItemEvent(eventId = EventId("\$M1"))
        val redacted = aTimelineItemEvent(eventId = EventId("\$RED"), content = TimelineItemRedactedContent)
        val m2 = aTimelineItemEvent(eventId = EventId("\$M2"))
        val presenter = createMessagesPresenter(
            timelineItems = persistentListOf(m1, redacted, m2),
        )
        presenter.testWithLifecycleOwner {
            val initial = awaitItem()
            initial.eventSink(MessagesEvent.ToggleSelection(m1))
            initial.eventSink(MessagesEvent.ToggleSelection(redacted))
            initial.eventSink(MessagesEvent.ToggleSelection(m2))
            val state = consumeItemsUntilPredicate { it.selectionState.count == 2 }.last()
            assertThat(state.selectionState.selectedIds).containsExactly(m1.eventId, m2.eventId)
            assertThat(state.selectionState.selectedIds).doesNotContain(redacted.eventId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `EnterSelection ignores non-bulk-selectable anchors and respects the cap`() = runTest {
        val redacted = aTimelineItemEvent(eventId = EventId("\$RED"), content = TimelineItemRedactedContent)
        val msg = aTimelineItemEvent(eventId = EventId("\$MSG"))
        val presenter = createMessagesPresenter(
            timelineItems = persistentListOf(redacted, msg),
        )
        presenter.testWithLifecycleOwner {
            val initial = awaitItem()
            initial.eventSink(MessagesEvent.EnterSelection(redacted))
            initial.eventSink(MessagesEvent.EnterSelection(msg))
            val state = consumeItemsUntilPredicate { it.selectionState.isActive }.last()
            assertThat(state.selectionState.selectedIds).containsExactly(msg.eventId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `BulkRedact - all success - no snackbar fires`() = runTest {
        val targetEvents = (0 until 3).map { aTimelineItemEvent(eventId = EventId("\$TGT-$it")) }
        val redactCalls = mutableListOf<EventOrTransactionId>()
        val timeline = FakeTimeline().apply {
            redactEventLambda = { eventOrTransactionId, _ ->
                redactCalls += eventOrTransactionId
                Result.success(Unit)
            }
        }
        val presenter = createMessagesPresenter(
            timeline = timeline,
            timelineItems = targetEvents.toImmutableList(),
        )
        presenter.testWithLifecycleOwner {
            val initial = awaitItem()
            targetEvents.forEach { initial.eventSink(MessagesEvent.ToggleSelection(it)) }
            val readied = consumeItemsUntilPredicate { it.selectionState.count == targetEvents.size }.last()
            assertThat(readied.selectionState.count).isEqualTo(targetEvents.size)
            readied.eventSink(MessagesEvent.BulkRedactSelected)
            // The redactions run in the background with a delay between each of them.
            val finalState = consumeItemsUntilTimeout(2.seconds).last()
            assertThat(redactCalls).hasSize(targetEvents.size)
            assertThat(finalState.selectionState.selectedIds).isEmpty()
            assertThat(finalState.snackbarMessage).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `BulkRedact also redacts selected messages that scrolled out of the loaded window`() = runTest {
        // Only `loaded` is in the timeline, `evicted` is selected but no longer loaded.
        val loaded = aTimelineItemEvent(eventId = EventId("\$LOADED"))
        val evicted = aTimelineItemEvent(eventId = EventId("\$EVICTED"))
        val redactCalls = mutableListOf<EventOrTransactionId>()
        val timeline = FakeTimeline().apply {
            redactEventLambda = { eventOrTransactionId, _ ->
                redactCalls += eventOrTransactionId
                Result.success(Unit)
            }
        }
        val presenter = createMessagesPresenter(
            timeline = timeline,
            timelineItems = persistentListOf(loaded),
        )
        presenter.testWithLifecycleOwner {
            val initial = awaitItem()
            initial.eventSink(MessagesEvent.ToggleSelection(loaded))
            initial.eventSink(MessagesEvent.ToggleSelection(evicted))
            val readied = consumeItemsUntilPredicate { it.selectionState.count == 2 }.last()
            readied.eventSink(MessagesEvent.BulkRedactSelected)
            consumeItemsUntilTimeout(2.seconds)
            assertThat(redactCalls).containsExactly(
                loaded.eventId!!.toEventOrTransactionId(),
                evicted.eventId!!.toEventOrTransactionId(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `BulkRedact - partial failure - snackbar fires with common_error`() = runTest {
        val targetEvents = (0 until 4).map { aTimelineItemEvent(eventId = EventId("\$TGT-$it")) }
        val timeline = FakeTimeline().apply {
            var idx = 0
            redactEventLambda = { _, _ ->
                val res = if (idx % 2 == 1) Result.failure(RuntimeException("boom")) else Result.success(Unit)
                idx += 1
                res
            }
        }
        val presenter = createMessagesPresenter(
            timeline = timeline,
            timelineItems = targetEvents.toImmutableList(),
        )
        presenter.testWithLifecycleOwner {
            val initial = awaitItem()
            targetEvents.forEach { initial.eventSink(MessagesEvent.ToggleSelection(it)) }
            val readied = consumeItemsUntilPredicate { it.selectionState.count == targetEvents.size }.last()
            readied.eventSink(MessagesEvent.BulkRedactSelected)
            advanceUntilIdle()
            val withSnackbar = consumeItemsUntilPredicate { it.snackbarMessage != null }.last()
            assertThat(withSnackbar.snackbarMessage?.messageResId).isEqualTo(CommonStrings.common_error)
            assertThat(withSnackbar.selectionState.selectedIds).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `BulkForward orders by sentTimeMillis ASC regardless of tap order`() = runTest {
        // sentTimeMillis order differs from both the timeline order and the tap order.
        val e1 = aTimelineItemEvent(eventId = EventId("\$E1")).copy(sentTimeMillis = 1000L)
        val e2 = aTimelineItemEvent(eventId = EventId("\$E2")).copy(sentTimeMillis = 2000L)
        val e3 = aTimelineItemEvent(eventId = EventId("\$E3")).copy(sentTimeMillis = 3000L)
        val items = persistentListOf<TimelineItem>(e2, e3, e1)
        val forwarded = mutableListOf<EventId>()
        val navigator = FakeMessagesNavigator(
            onForwardEventClickLambda = { id -> forwarded += id },
        )
        val presenter = createMessagesPresenter(
            navigator = navigator,
            timelineItems = items,
        )
        presenter.testWithLifecycleOwner {
            val initial = awaitItem()
            initial.eventSink(MessagesEvent.ToggleSelection(e3))
            initial.eventSink(MessagesEvent.ToggleSelection(e1))
            initial.eventSink(MessagesEvent.ToggleSelection(e2))
            val readied = consumeItemsUntilPredicate { it.selectionState.count == 3 }.last()
            readied.eventSink(MessagesEvent.BulkForwardSelected)
            advanceUntilIdle()
            assertThat(forwarded).containsExactly(e1.eventId, e2.eventId, e3.eventId).inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `BulkCopySelected joins bodies in chronological order and clears selection`() = runTest {
        val e1 = aTimelineItemEvent(eventId = EventId("\$C1"), content = aTimelineItemTextContent(body = "first")).copy(sentTimeMillis = 1000L)
        val e2 = aTimelineItemEvent(eventId = EventId("\$C2"), content = aTimelineItemTextContent(body = "second")).copy(sentTimeMillis = 2000L)
        val items = persistentListOf<TimelineItem>(e2, e1)
        val clipboardHelper = FakeClipboardHelper()
        val presenter = createMessagesPresenter(
            clipboardHelper = clipboardHelper,
            timelineItems = items,
        )
        presenter.testWithLifecycleOwner {
            val initial = awaitItem()
            initial.eventSink(MessagesEvent.ToggleSelection(e2))
            initial.eventSink(MessagesEvent.ToggleSelection(e1))
            val readied = consumeItemsUntilPredicate { it.selectionState.count == 2 }.last()
            readied.eventSink(MessagesEvent.BulkCopySelected)
            val finalState = consumeItemsUntilPredicate { !it.selectionState.isActive }.last()
            assertThat(clipboardHelper.clipboardContents).isEqualTo("first\n\nsecond")
            assertThat(finalState.selectionState.selectedIds).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `BulkCopySelected includes media captions alongside text bodies`() = runTest {
        val text = aTimelineItemEvent(eventId = EventId("\$T"), content = aTimelineItemTextContent(body = "foo")).copy(sentTimeMillis = 1000L)
        val image = aTimelineItemEvent(eventId = EventId("\$I"), content = aTimelineItemImageContent(caption = "bar")).copy(sentTimeMillis = 2000L)
        val clipboardHelper = FakeClipboardHelper()
        val presenter = createMessagesPresenter(
            clipboardHelper = clipboardHelper,
            timelineItems = persistentListOf<TimelineItem>(text, image),
        )
        presenter.testWithLifecycleOwner {
            val initial = awaitItem()
            initial.eventSink(MessagesEvent.ToggleSelection(image))
            initial.eventSink(MessagesEvent.ToggleSelection(text))
            val readied = consumeItemsUntilPredicate { it.selectionState.count == 2 }.last()
            readied.eventSink(MessagesEvent.BulkCopySelected)
            consumeItemsUntilPredicate { !it.selectionState.isActive }
            assertThat(clipboardHelper.clipboardContents).isEqualTo("foo\n\nbar")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `BulkCopySelected with nothing to copy keeps the selection`() = runTest {
        // Nothing to copy, so the selection must survive for Forward or Delete.
        val image = aTimelineItemEvent(eventId = EventId("\$IMG"), content = aTimelineItemImageContent())
        val other = aTimelineItemEvent(eventId = EventId("\$IMG2"), content = aTimelineItemImageContent())
        val clipboardHelper = FakeClipboardHelper()
        val presenter = createMessagesPresenter(
            clipboardHelper = clipboardHelper,
            timelineItems = persistentListOf(image, other),
        )
        presenter.testWithLifecycleOwner {
            val initial = awaitItem()
            initial.eventSink(MessagesEvent.ToggleSelection(image))
            val readied = consumeItemsUntilPredicate { it.selectionState.count == 1 }.last()
            readied.eventSink(MessagesEvent.BulkCopySelected)
            // Copy emits no new state, so toggle a second message to force an emission.
            readied.eventSink(MessagesEvent.ToggleSelection(other))
            val state = consumeItemsUntilPredicate { it.selectionState.count == 2 }.last()
            assertThat(clipboardHelper.clipboardContents).isNull()
            assertThat(state.selectionState.selectedIds).containsExactly(image.eventId, other.eventId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ClearSelection exits selection mode`() = runTest {
        val msg = aTimelineItemEvent(eventId = EventId("\$M"))
        val presenter = createMessagesPresenter(timelineItems = persistentListOf(msg))
        presenter.testWithLifecycleOwner {
            val initial = awaitItem()
            initial.eventSink(MessagesEvent.ToggleSelection(msg))
            val active = consumeItemsUntilPredicate { it.selectionState.isActive }.last()
            assertThat(active.selectionState.isActive).isTrue()
            active.eventSink(MessagesEvent.ClearSelection)
            val cleared = consumeItemsUntilPredicate { !it.selectionState.isActive }.last()
            assertThat(cleared.selectionState.selectedIds).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `HandleAction Select enters selection mode`() = runTest {
        val msg = aTimelineItemEvent(eventId = EventId("\$SEL"))
        val presenter = createMessagesPresenter(timelineItems = persistentListOf(msg))
        presenter.testWithLifecycleOwner {
            val initial = awaitItem()
            initial.eventSink(MessagesEvent.HandleAction(TimelineItemAction.Select, msg))
            val state = consumeItemsUntilPredicate { it.selectionState.isActive }.last()
            assertThat(state.selectionState.selectedIds).containsExactly(msg.eventId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ToggleSelection silently rejects beyond the cap - no snackbar, the counter shows the limit`() = runTest {
        val cap = TimelineSelectionState.MAX_SELECTION
        val items = (0 until cap + 1).map { aTimelineItemEvent(eventId = EventId("\$E-$it")) }
        val presenter = createMessagesPresenter(
            timelineItems = items.toImmutableList(),
        )
        presenter.testWithLifecycleOwner {
            val initial = awaitItem()
            items.take(cap).forEach { initial.eventSink(MessagesEvent.ToggleSelection(it)) }
            val full = consumeItemsUntilPredicate { it.selectionState.count == cap }.last()
            assertThat(full.selectionState.isAtCap).isTrue()
            assertThat(full.snackbarMessage).isNull()
            // The extra tap emits no new state, so deselect one to force an emission.
            full.eventSink(MessagesEvent.ToggleSelection(items.last()))
            full.eventSink(MessagesEvent.ToggleSelection(items.first()))
            val after = consumeItemsUntilPredicate { it.selectionState.count == cap - 1 }.last()
            assertThat(after.selectionState.selectedIds).doesNotContain(items.last().eventId)
            assertThat(after.snackbarMessage).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `BulkSaveSelected saves every selected file and clears the selection`() = runTest {
        val photo = aTimelineItemEvent(eventId = EventId("\$S1"), content = aTimelineItemImageContent(filename = "photo.jpg")).copy(sentTimeMillis = 1000L)
        val document = aTimelineItemEvent(eventId = EventId("\$S2"), content = aTimelineItemFileContent(fileName = "notes.pdf")).copy(sentTimeMillis = 2000L)
        val selectionMediaSaver = FakeSelectionMediaSaver()
        val presenter = createMessagesPresenter(
            timelineItems = persistentListOf(document, photo),
            selectionMediaSaver = selectionMediaSaver,
        )
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            initialState.eventSink(MessagesEvent.ToggleSelection(document))
            initialState.eventSink(MessagesEvent.ToggleSelection(photo))
            val state = consumeItemsUntilPredicate { it.selectionState.count == 2 }.last()
            assertThat(state.selectionState.canSave).isTrue()
            state.eventSink(MessagesEvent.BulkSaveSelected)
            val savingState = consumeItemsUntilPredicate { it.selectionSaveProgress != null }.last()
            assertThat(savingState.selectionState.isActive).isFalse()
            assertThat(savingState.selectionSaveProgress?.total).isEqualTo(2)
            advanceUntilIdle()
            consumeItemsUntilPredicate { it.selectionSaveProgress == null }
            assertThat(selectionMediaSaver.savedFilenames).containsExactly("photo.jpg", "notes.pdf").inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `BulkSaveSelected does nothing when the selection contains a text message`() = runTest {
        val photo = aTimelineItemEvent(eventId = EventId("\$S3"), content = aTimelineItemImageContent())
        val text = aTimelineItemEvent(eventId = EventId("\$S4"), content = aTimelineItemTextContent(body = "hello"))
        val selectionMediaSaver = FakeSelectionMediaSaver()
        val presenter = createMessagesPresenter(
            timelineItems = persistentListOf(photo, text),
            selectionMediaSaver = selectionMediaSaver,
        )
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            initialState.eventSink(MessagesEvent.ToggleSelection(photo))
            initialState.eventSink(MessagesEvent.ToggleSelection(text))
            val state = consumeItemsUntilPredicate { it.selectionState.count == 2 }.last()
            assertThat(state.selectionState.canSave).isFalse()
            state.eventSink(MessagesEvent.BulkSaveSelected)
            advanceUntilIdle()
            assertThat(selectionMediaSaver.savedFilenames).isEmpty()
            assertThat(state.selectionState.count).isEqualTo(2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `CancelSelectionSave stops the batch`() = runTest {
        val first = aTimelineItemEvent(eventId = EventId("\$S5"), content = aTimelineItemImageContent(filename = "one.jpg")).copy(sentTimeMillis = 1000L)
        val second = aTimelineItemEvent(eventId = EventId("\$S6"), content = aTimelineItemImageContent(filename = "two.jpg")).copy(sentTimeMillis = 2000L)
        val selectionMediaSaver = FakeSelectionMediaSaver()
        val presenter = createMessagesPresenter(
            timelineItems = persistentListOf(first, second),
            selectionMediaSaver = selectionMediaSaver,
        )
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            initialState.eventSink(MessagesEvent.ToggleSelection(first))
            initialState.eventSink(MessagesEvent.ToggleSelection(second))
            val state = consumeItemsUntilPredicate { it.selectionState.count == 2 }.last()
            state.eventSink(MessagesEvent.BulkSaveSelected)
            val savingState = consumeItemsUntilPredicate { it.selectionSaveProgress != null }.last()
            savingState.eventSink(MessagesEvent.CancelSelectionSave)
            val cancelledState = consumeItemsUntilPredicate { it.selectionSaveProgress == null }.last()
            advanceUntilIdle()
            assertThat(cancelledState.selectionSaveProgress).isNull()
            assertThat(selectionMediaSaver.savedFilenames.size).isLessThan(2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun TestScope.createMessagesPresenter(
        coroutineDispatchers: CoroutineDispatchers = testCoroutineDispatchers(),
        timeline: Timeline = FakeTimeline(),
        joinedRoom: FakeJoinedRoom = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                roomPermissions = FakeRoomPermissions(
                    canSendState = { type ->
                        when (type) {
                            StateEventType.CallMember -> true
                            else -> lambdaError()
                        }
                    },
                    canSendMessage = { type ->
                        when (type) {
                            MessageEventType.RoomMessage -> true
                            MessageEventType.Reaction -> true
                            else -> lambdaError()
                        }
                    },
                    canRedactOther = true,
                    canRedactOwn = true,
                    canPinUnpin = true,
                ),
            ).apply {
                givenRoomInfo(aRoomInfo(id = roomId, name = ""))
            },
            liveTimeline = timeline,
            typingNoticeResult = { Result.success(Unit) },
        ),
        navigator: FakeMessagesNavigator = FakeMessagesNavigator(),
        clipboardHelper: FakeClipboardHelper = FakeClipboardHelper(),
        selectionMediaSaver: FakeSelectionMediaSaver = FakeSelectionMediaSaver(),
        analyticsService: FakeAnalyticsService = FakeAnalyticsService(),
        timelineItems: ImmutableList<TimelineItem> = persistentListOf(),
        timelineEventSink: (TimelineEvent) -> Unit = {},
        permalinkParser: PermalinkParser = FakePermalinkParser(),
        messageComposerPresenter: Presenter<MessageComposerState> = Presenter {
            aMessageComposerState(
                textEditorState = aTextEditorStateMarkdown(initialText = "", initialFocus = false)
            )
        },
        roomMemberModerationPresenter: Presenter<RoomMemberModerationState> = Presenter {
            aRoomMemberModerationState()
        },
        encryptionService: FakeEncryptionService = FakeEncryptionService(),
        featureFlagService: FakeFeatureFlagService = FakeFeatureFlagService(),
        actionListEventSink: (ActionListEvent) -> Unit = {},
        addRecentEmoji: AddRecentEmoji = AddRecentEmoji { _ -> lambdaError() },
        markAsFullyRead: MarkAsFullyRead = FakeMarkAsFullyRead(),
        liveLocationShareManager: FakeActiveLiveLocationShareManager = FakeActiveLiveLocationShareManager(),
    ): MessagesPresenter {
        return MessagesPresenter(
            navigator = navigator,
            room = joinedRoom,
            composerPresenter = messageComposerPresenter,
            voiceMessageComposerPresenterFactory = FakeDefaultVoiceMessageComposerPresenterFactory(backgroundScope),
            timelinePresenter = { aTimelineState(timelineItems = timelineItems, eventSink = timelineEventSink) },
            timelineProtectionPresenter = { aTimelineProtectionState() },
            identityChangeStatePresenter = { anIdentityChangeState() },
            linkPresenter = { aLinkState() },
            actionListPresenter = { anActionListState(eventSink = actionListEventSink) },
            customReactionPresenter = { aCustomReactionState() },
            reactionSummaryPresenter = { aReactionSummaryState() },
            readReceiptBottomSheetPresenter = { aReadReceiptBottomSheetState() },
            pinnedMessagesBannerPresenter = { aLoadedPinnedMessagesBannerState() },
            roomCallStatePresenter = { aStandByCallState() },
            roomMemberModerationPresenter = roomMemberModerationPresenter,
            snackbarDispatcher = SnackbarDispatcher(),
            dispatchers = coroutineDispatchers,
            clipboardHelper = clipboardHelper,
            selectionMediaSaver = selectionMediaSaver,
            htmlConverterProvider = FakeHtmlConverterProvider(),
            buildMeta = aBuildMeta(),
            timelineController = TimelineController(joinedRoom, timeline),
            permalinkParser = permalinkParser,
            analyticsService = analyticsService,
            encryptionService = encryptionService,
            featureFlagService = featureFlagService,
            addRecentEmoji = addRecentEmoji,
            markAsFullyRead = markAsFullyRead,
            liveLocationShareManager = liveLocationShareManager,
            sessionCoroutineScope = backgroundScope,
        )
    }
}
