package io.element.android.x.features.login.changeserver

sealed interface ChangeServerEvents {
    data class SetServer(val server: String) : ChangeServerEvents
    object Submit: ChangeServerEvents
}
