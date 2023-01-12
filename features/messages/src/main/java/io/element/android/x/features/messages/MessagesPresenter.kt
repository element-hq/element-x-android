package io.element.android.x.features.messages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.x.architecture.Presenter
import io.element.android.x.features.messages.actionlist.ActionListPresenter
import io.element.android.x.features.messages.actionlist.TimelineItemAction
import io.element.android.x.features.messages.model.MessagesTimelineItemState
import io.element.android.x.features.messages.model.content.MessagesTimelineItemTextBasedContent
import io.element.android.x.features.messages.textcomposer.MessageComposerEvents
import io.element.android.x.features.messages.textcomposer.MessageComposerPresenter
import io.element.android.x.features.messages.textcomposer.MessageComposerState
import io.element.android.x.features.messages.timeline.TimelinePresenter
import io.element.android.x.matrix.MatrixClient
import io.element.android.x.matrix.core.RoomId
import io.element.android.x.matrix.room.MatrixRoom
import io.element.android.x.textcomposer.MessageComposerMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MessagesPresenter @Inject constructor(
    private val client: MatrixClient,
    private val roomId: RoomId,
    private val room: MatrixRoom,
    private val composerPresenter: MessageComposerPresenter,
    private val timelinePresenter: TimelinePresenter,
    private val actionListPresenter: ActionListPresenter,
) : Presenter<MessagesState> {

    @Composable
    override fun present(): MessagesState {
        val localCoroutineScope = rememberCoroutineScope()
        val composerState = composerPresenter.present()
        val timelineState = timelinePresenter.present()
        val actionListState = actionListPresenter.present()

        fun handleEvents(event: MessagesEvents) {
            when (event) {
                is MessagesEvents.HandleAction -> localCoroutineScope.handleTimelineAction(event.action, event.messageEvent, composerState)
            }
        }
        return MessagesState(
            roomId = roomId,
            composerState = composerState,
            timelineState = timelineState,
            actionListState = actionListState,
            eventSink = ::handleEvents
        )
    }

    fun CoroutineScope.handleTimelineAction(
        action: TimelineItemAction,
        targetEvent: MessagesTimelineItemState.MessageEvent,
        composerState: MessageComposerState,
    ) = launch {
        when (action) {
            TimelineItemAction.Copy -> notImplementedYet()
            TimelineItemAction.Forward -> notImplementedYet()
            TimelineItemAction.Redact -> handleActionRedact(targetEvent)
            TimelineItemAction.Edit -> handleActionEdit(targetEvent, composerState)
            TimelineItemAction.Reply -> handleActionReply(targetEvent, composerState)
        }
    }

    private fun notImplementedYet() {
        Timber.v("NotImplementedYet")
    }

    private suspend fun handleActionRedact(event: MessagesTimelineItemState.MessageEvent) {
        room.redactEvent(event.id)
    }

    private fun handleActionEdit(targetEvent: MessagesTimelineItemState.MessageEvent, composerState: MessageComposerState) {
        val composerMode = MessageComposerMode.Edit(
            targetEvent.id,
            (targetEvent.content as? MessagesTimelineItemTextBasedContent)?.body.orEmpty()
        )
        composerState.eventSink(
            MessageComposerEvents.SetMode(composerMode)
        )
    }

    private fun handleActionReply(targetEvent: MessagesTimelineItemState.MessageEvent, composerState: MessageComposerState) {
        val composerMode = MessageComposerMode.Reply(targetEvent.safeSenderName, targetEvent.id, "")
        composerState.eventSink(
            MessageComposerEvents.SetMode(composerMode)
        )
    }
}
