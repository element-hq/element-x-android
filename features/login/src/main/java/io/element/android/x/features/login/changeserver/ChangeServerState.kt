package io.element.android.x.features.login.changeserver

import io.element.android.x.architecture.Async

data class ChangeServerState(
    val homeserver: String = "",
    val changeServerAction: Async<Unit> = Async.Uninitialized,
) {
    val submitEnabled = homeserver.isNotEmpty() && changeServerAction !is Async.Loading
}
