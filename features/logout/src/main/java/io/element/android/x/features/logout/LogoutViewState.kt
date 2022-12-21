package io.element.android.x.features.logout

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized

data class LogoutViewState(
    val logoutAction: Async<Unit> = Uninitialized,
) : MavericksState
