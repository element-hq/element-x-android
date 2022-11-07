package io.element.android.x.features.messages

import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import io.element.android.x.designsystem.components.avatar.AvatarData
import io.element.android.x.designsystem.components.avatar.AvatarSize
import io.element.android.x.features.messages.model.MessagesViewState
import io.element.android.x.matrix.MatrixClient
import io.element.android.x.matrix.MatrixInstance
import io.element.android.x.matrix.room.MatrixRoom
import io.element.android.x.matrix.timeline.MatrixTimeline
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.matrix.rustcomponents.sdk.mediaSourceFromUrl

private const val PAGINATION_COUNT = 50
class MessagesViewModel(
    private val client: MatrixClient,
    private val room: MatrixRoom,
    private val timeline: MatrixTimeline,
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
            return MessagesViewModel(client, room, room.timeline(), state)
        }

    }


    init {
        handleInit()
    }

    fun loadMore(){
        viewModelScope.launch {
            timeline.paginateBackwards(PAGINATION_COUNT)
            setState { copy(hasMoreToLoad = timeline.hasMoreToLoad) }
        }
    }

    private fun handleInit() {
        setState {
            copy(hasMoreToLoad = timeline.hasMoreToLoad)
        }

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
            .execute {
                copy(timelineItems = it)
            }
    }

    private suspend fun loadAvatarData(
        name: String,
        url: String?,
        size: AvatarSize = AvatarSize.MEDIUM
    ): AvatarData {
        val mediaContent = url?.let {
            val mediaSource = mediaSourceFromUrl(it)
            client.loadMediaThumbnailForSource(
                mediaSource,
                size.value.toLong(),
                size.value.toLong()
            )
        }
        return mediaContent?.fold(
            { it },
            { null }
        ).let { model ->
            AvatarData(name.first().uppercase(), model, size)
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}