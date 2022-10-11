package io.element.android.x.ui.screen.login

import androidx.lifecycle.ViewModel
import io.element.android.x.sdk.matrix.MatrixInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RoomListViewModel : ViewModel() {

    private val matrix = MatrixInstance.getInstance()

    private val _state = MutableStateFlow(RoomListViewState())
    val state = _state.asStateFlow()

    init {
        observeState()
    }

    private fun observeState() {
        // TODO Update submitEnabled when other state members are updated.
    }

    fun handle(action: RoomListActions) {
        when (action) {
            RoomListActions.LoadMore -> TODO()
        }
    }
}