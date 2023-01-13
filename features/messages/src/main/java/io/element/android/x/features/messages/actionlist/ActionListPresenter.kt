package io.element.android.x.features.messages.actionlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.x.architecture.Presenter
import io.element.android.x.features.messages.actionlist.model.TimelineItemAction
import io.element.android.x.features.messages.timeline.model.TimelineItem
import io.element.android.x.features.messages.timeline.model.content.TimelineItemRedactedContent
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

    fun CoroutineScope.computeForMessage(timelineItem: TimelineItem.MessageEvent, target: MutableState<ActionListState.Target>) = launch {
        target.value = ActionListState.Target.Loading(timelineItem)
        val actions =
            if (timelineItem.content is TimelineItemRedactedContent) {
                emptyList()
            } else {
                mutableListOf(
                    TimelineItemAction.Reply,
                    TimelineItemAction.Forward,
                    TimelineItemAction.Copy,
                ).also {
                    if (timelineItem.isMine) {
                        it.add(TimelineItemAction.Edit)
                        it.add(TimelineItemAction.Redact)
                    }
                }
            }
        target.value = ActionListState.Target.Success(timelineItem, actions.toImmutableList())
    }
}
