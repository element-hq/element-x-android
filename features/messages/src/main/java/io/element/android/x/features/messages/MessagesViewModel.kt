package io.element.android.x.features.messages

import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.Success
import io.element.android.x.core.data.parallelMap
import io.element.android.x.designsystem.components.avatar.AvatarData
import io.element.android.x.designsystem.components.avatar.AvatarSize
import io.element.android.x.features.messages.model.MessagesViewState
import io.element.android.x.matrix.MatrixClient
import io.element.android.x.matrix.MatrixInstance
import io.element.android.x.matrix.room.RoomSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.matrix.rustcomponents.sdk.mediaSourceFromUrl

class MessagesViewModel(initialState: MessagesViewState) :
    MavericksViewModel<MessagesViewState>(initialState) {

    private val matrix = MatrixInstance.getInstance()

    init {
        handleInit()
    }

    private fun handleInit() {
        viewModelScope.launch {


        }
    }

    private suspend fun loadAvatarData(
        client: MatrixClient,
        name: String,
        url: String?,
        size: AvatarSize = AvatarSize.MEDIUM
    ): AvatarData {
        val mediaContent = url?.let {
            val mediaSource = mediaSourceFromUrl(it)
            client.loadMediaThumbnailForSource(mediaSource, size.value.toLong(), size.value.toLong())
        }
        return mediaContent?.fold(
            { it },
            { null }
        ).let { model ->
            AvatarData(name.first().uppercase(), model, size)
        }
    }

    private suspend fun getClient(): MatrixClient {
        return matrix.matrixClient().first().get()
    }

    override fun onCleared() {
        super.onCleared()
    }
}