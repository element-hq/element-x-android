package io.element.android.x.features.messages

import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import io.element.android.x.designsystem.components.avatar.AvatarData
import io.element.android.x.designsystem.components.avatar.AvatarSize
import io.element.android.x.features.messages.model.MessagesItemAction
import io.element.android.x.features.messages.model.MessagesItemActionsSheetState
import io.element.android.x.features.messages.model.MessagesTimelineItemState
import io.element.android.x.features.messages.model.MessagesViewState
import io.element.android.x.features.messages.model.content.MessagesTimelineItemRedactedContent
import io.element.android.x.features.messages.model.content.MessagesTimelineItemTextBasedContent
import io.element.android.x.matrix.MatrixClient
import io.element.android.x.matrix.MatrixInstance
import io.element.android.x.matrix.media.MediaResolver
import io.element.android.x.matrix.room.MatrixRoom
import io.element.android.x.matrix.timeline.MatrixTimeline
import io.element.android.x.textcomposer.MessageComposerMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

private const val PAGINATION_COUNT = 50

class MessagesViewModel(
    private val client: MatrixClient,
    private val room: MatrixRoom,
    private val timeline: MatrixTimeline,
    private val messageTimelineItemStateMapper: MessageTimelineItemStateMapper,
    private val initialState: MessagesViewState
) :
    MavericksViewModel<MessagesViewState>(initialState) {

    companion object : MavericksViewModelFactory<MessagesViewModel, MessagesViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: MessagesViewState
        ): MessagesViewModel? {
            val matrix = MatrixInstance.getInstance()
            val client = matrix.activeClient()
            val room = client.getRoom(state.roomId) ?: return null
            val messageTimelineItemStateMapper =
                MessageTimelineItemStateMapper(client, room, Dispatchers.Default)
            return MessagesViewModel(
                client,
                room,
                room.timeline(),
                messageTimelineItemStateMapper,
                state
            )
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
        withState { state ->
            viewModelScope.launch {
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
                // Reset composer
                setNormalMode()
            }
        }
    }

    suspend fun getTargetEvent(): MessagesTimelineItemState.MessageEvent? {
        val currentState = awaitState()
        return currentState.itemActionsSheetState.invoke()?.targetItem
    }

    fun handleItemAction(action: MessagesItemAction) {
        viewModelScope.launch(Dispatchers.Default) {
            val currentState = awaitState()
            Timber.v("Handle $action for ${currentState.itemActionsSheetState}")
            val targetEvent = getTargetEvent()
                ?: return@launch
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
                composerMode = mode
            )
        }
    }

    private fun notImplementedYet() {
        setSnackbarContent("Not implemented yet!")
    }

    fun onSnackbarShown() {
        setSnackbarContent(null)
    }

    private fun setSnackbarContent(message: String?) {
        setState { copy(snackbarContent = message) }
    }

    private fun handleActionRedact(event: MessagesTimelineItemState.MessageEvent) {
        viewModelScope.launch {
            room.redactEvent(event.id)
        }
    }

    fun computeActionsSheetState(messagesTimelineItemState: MessagesTimelineItemState.MessageEvent) {
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

        combine(
            timeline.timelineItems(),
            stateFlow
                .map { it.composerMode }
                .distinctUntilChanged()
        ) { timelineItems, messageComposerMode ->
            // Set the highlightedEventId to messageTimelineItemStateMapper, before the mapping occurs
            messageTimelineItemStateMapper.highlightedEventId = when (messageComposerMode) {
                is MessageComposerMode.Normal -> null
                is MessageComposerMode.Edit -> messageComposerMode.eventId
                is MessageComposerMode.Quote -> null
                is MessageComposerMode.Reply -> messageComposerMode.eventId
            }
            timelineItems
        }
            .map(messageTimelineItemStateMapper::map)
            .execute {
                copy(timelineItems = it)
            }
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
        timeline.dispose()
    }
}