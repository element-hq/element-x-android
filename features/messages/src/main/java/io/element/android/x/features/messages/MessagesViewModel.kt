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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.matrix.rustcomponents.sdk.mediaSourceFromUrl

class MessagesViewModel(
    private val client: MatrixClient,
    private val room: MatrixRoom,
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
            return MessagesViewModel(client, room, state)
        }

    }


    init {
        handleInit()
    }

    private fun handleInit() {
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