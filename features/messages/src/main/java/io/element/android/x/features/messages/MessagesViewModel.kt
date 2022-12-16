package io.element.android.x.features.messages

import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Uninitialized
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.x.anvilannotations.ContributesViewModel
import io.element.android.x.core.di.daggerMavericksViewModelFactory
import io.element.android.x.designsystem.components.avatar.AvatarData
import io.element.android.x.designsystem.components.avatar.AvatarSize
import io.element.android.x.di.AppScope
import io.element.android.x.di.SessionScope
import io.element.android.x.features.messages.model.MessagesItemAction
import io.element.android.x.features.messages.model.MessagesItemActionsSheetState
import io.element.android.x.features.messages.model.MessagesTimelineItemState
import io.element.android.x.features.messages.model.MessagesViewState
import io.element.android.x.features.messages.model.content.MessagesTimelineItemRedactedContent
import io.element.android.x.features.messages.model.content.MessagesTimelineItemTextBasedContent
import io.element.android.x.matrix.Matrix
import io.element.android.x.matrix.MatrixClient
import io.element.android.x.matrix.media.MediaResolver
import io.element.android.x.matrix.timeline.MatrixTimeline
import io.element.android.x.matrix.timeline.MatrixTimelineItem
import io.element.android.x.textcomposer.MessageComposerMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val PAGINATION_COUNT = 50

@ContributesViewModel(SessionScope::class)
class MessagesViewModel @AssistedInject constructor(
    private val client: MatrixClient,
    @Assisted private val initialState: MessagesViewState
) :
    MavericksViewModel<MessagesViewState>(initialState) {

    companion object : MavericksViewModelFactory<MessagesViewModel, MessagesViewState> by daggerMavericksViewModelFactory()

    private val room = client.getRoom(initialState.roomId)!!
    private val messageTimelineItemStateFactory =
        MessageTimelineItemStateFactory(client, room, Dispatchers.Default)
    private val timeline = room.timeline()

    private val timelineCallback = object : MatrixTimeline.Callback {
        override fun onPushedTimelineItem(timelineItem: MatrixTimelineItem) {
            viewModelScope.launch {
                messageTimelineItemStateFactory.pushItem(timelineItem)
            }
        }
    }

    init {
        handleInit()
    }

    fun loadMore() {
        viewModelScope.launch {
            timeline.paginateBackwards(PAGINATION_COUNT)
            setState { copy(hasMoreToLoad = timeline.hasMoreToLoad) }
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            val state = awaitState()
            // Reset composer right away
            setNormalMode()
            when (state.composerMode) {
                is MessageComposerMode.Normal -> timeline.sendMessage(text)
                is MessageComposerMode.Edit -> timeline.editMessage(
                    state.composerMode.eventId,
                    text
                )
                is MessageComposerMode.Quote -> TODO()
                is MessageComposerMode.Reply -> timeline.replyMessage(
                    state.composerMode.eventId,
                    text
                )
            }
        }
    }

    suspend fun getTargetEvent(): MessagesTimelineItemState.MessageEvent? {
        val currentState = awaitState()
        return currentState.itemActionsSheetState.invoke()?.targetItem
    }

    fun handleItemAction(
        action: MessagesItemAction,
        targetEvent: MessagesTimelineItemState.MessageEvent
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            when (action) {
                MessagesItemAction.Copy -> notImplementedYet()
                MessagesItemAction.Forward -> notImplementedYet()
                MessagesItemAction.Redact -> handleActionRedact(targetEvent)
                MessagesItemAction.Edit -> handleActionEdit(targetEvent)
                MessagesItemAction.Reply -> handleActionReply(targetEvent)
            }
        }
    }

    fun setNormalMode() {
        setComposerMode(MessageComposerMode.Normal(""))
    }

    fun onSnackbarShown() {
        setSnackbarContent(null)
    }

    fun computeActionsSheetState(messagesTimelineItemState: MessagesTimelineItemState.MessageEvent?) {
        if (messagesTimelineItemState == null) {
            setState { copy(itemActionsSheetState = Uninitialized) }
            return
        }
        suspend {
            val actions =
                if (messagesTimelineItemState.content is MessagesTimelineItemRedactedContent) {
                    emptyList()
                } else {
                    mutableListOf(
                        MessagesItemAction.Reply,
                        MessagesItemAction.Forward,
                        MessagesItemAction.Copy,
                    ).also {
                        if (messagesTimelineItemState.isMine) {
                            it.add(MessagesItemAction.Edit)
                            it.add(MessagesItemAction.Redact)
                        }
                    }
                }
            MessagesItemActionsSheetState(
                targetItem = messagesTimelineItemState,
                actions = actions
            )
        }.execute(Dispatchers.Default) {
            copy(itemActionsSheetState = it)
        }
    }

    private fun handleInit() {
        timeline.initialize()
        timeline.callback = timelineCallback
        room.syncUpdateFlow()
            .onEach {
                val avatarData =
                    loadAvatarData(room.name ?: room.roomId.value, room.avatarUrl, AvatarSize.SMALL)
                setState {
                    copy(
                        roomName = room.name, roomAvatar = avatarData,
                    )
                }
            }.launchIn(viewModelScope)

        timeline
            .timelineItems()
            .onEach(messageTimelineItemStateFactory::replaceWith)
            .launchIn(viewModelScope)

        messageTimelineItemStateFactory
            .flow()
            .execute {
                copy(timelineItems = it)
            }
    }

    private fun setSnackbarContent(message: String?) {
        setState { copy(snackbarContent = message) }
    }

    private fun handleActionRedact(event: MessagesTimelineItemState.MessageEvent) {
        viewModelScope.launch {
            room.redactEvent(event.id)
        }
    }

    private fun handleActionEdit(targetEvent: MessagesTimelineItemState.MessageEvent) {
        setComposerMode(
            MessageComposerMode.Edit(
                targetEvent.id,
                (targetEvent.content as? MessagesTimelineItemTextBasedContent)?.body.orEmpty()
            )
        )
    }

    private fun handleActionReply(targetEvent: MessagesTimelineItemState.MessageEvent) {
        setComposerMode(MessageComposerMode.Reply(targetEvent.safeSenderName, targetEvent.id, ""))
    }

    private fun setComposerMode(mode: MessageComposerMode) {
        setState {
            copy(
                composerMode = mode,
                highlightedEventId = mode.relatedEventId
            )
        }
    }

    private fun notImplementedYet() {
        setSnackbarContent("Not implemented yet!")
    }

    private suspend fun loadAvatarData(
        name: String,
        url: String?,
        size: AvatarSize = AvatarSize.MEDIUM
    ): AvatarData {
        val model = client.mediaResolver()
            .resolve(url, kind = MediaResolver.Kind.Thumbnail(size.value))
        return AvatarData(name, model, size)
    }

    override fun onCleared() {
        super.onCleared()
        timeline.callback = null
        timeline.dispose()
    }
}