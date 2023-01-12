package io.element.android.x.features.messages.actionlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.x.architecture.Presenter
import io.element.android.x.features.messages.model.MessagesTimelineItemState
import io.element.android.x.features.messages.model.content.MessagesTimelineItemRedactedContent
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ActionListPresenter @Inject constructor() : Presenter<ActionListState> {

    @Composable
    override fun present(): ActionListState {

        val localCoroutineScope = rememberCoroutineScope()

        val target: MutableState<ActionListState.Target> = remember {
            mutableStateOf(ActionListState.Target.None)
        }

        fun handleEvents(event: ActionListEvents) {
            when (event) {
                ActionListEvents.Clear -> target.value = ActionListState.Target.None
                is ActionListEvents.ComputeForMessage -> localCoroutineScope.computeForMessage(event.messageEvent, target)
            }
        }

        return ActionListState(
            target = target.value,
            eventSink = ::handleEvents
        )
    }

    fun CoroutineScope.computeForMessage(messagesTimelineItemState: MessagesTimelineItemState.MessageEvent, target: MutableState<ActionListState.Target>) = launch {
        target.value = ActionListState.Target.Loading(messagesTimelineItemState)
        val actions =
            if (messagesTimelineItemState.content is MessagesTimelineItemRedactedContent) {
                emptyList()
            } else {
                mutableListOf(
                    TimelineItemAction.Reply,
                    TimelineItemAction.Forward,
                    TimelineItemAction.Copy,
                ).also {
                    if (messagesTimelineItemState.isMine) {
                        it.add(TimelineItemAction.Edit)
                        it.add(TimelineItemAction.Redact)
                    }
                }
            }
        target.value = ActionListState.Target.Success(messagesTimelineItemState, actions.toImmutableList())
    }
}
