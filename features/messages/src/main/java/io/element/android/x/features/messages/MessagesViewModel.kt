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
import io.element.android.x.matrix.MatrixClient
import io.element.android.x.matrix.MatrixInstance
import io.element.android.x.matrix.media.MediaResolver
import io.element.android.x.matrix.room.MatrixRoom
import io.element.android.x.matrix.timeline.MatrixTimeline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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
        viewModelScope.launch {
            timeline.sendMessage(text)
        }
    }

    fun handleItemAction(action: MessagesItemAction) {
        viewModelScope.launch(Dispatchers.Default) {
            val currentState = awaitState()
            Timber.v("Handle $action for ${currentState.itemActionsSheetState}")
            val targetEvent =
                currentState.itemActionsSheetState.invoke()?.targetItem ?: return@launch
            when (action) {
                MessagesItemAction.Copy -> notImplementedYet()
                MessagesItemAction.Forward -> notImplementedYet()
                MessagesItemAction.Redact -> handleActionRedact(targetEvent)
            }
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
            val actions = mutableListOf(
                MessagesItemAction.Forward,
                MessagesItemAction.Copy,
            )
            if (messagesTimelineItemState.isMine) {
                actions.add(MessagesItemAction.Redact)
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

        timeline.timelineItems()
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