package io.element.android.x.features.login.changeserver

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized

data class ChangeServerViewState(
    val homeserver: String = "",
    val changeServerAction: Async<Unit> = Uninitialized,
) : MavericksState {
    val submitEnabled = homeserver.isNotEmpty() && changeServerAction !is Loading
}