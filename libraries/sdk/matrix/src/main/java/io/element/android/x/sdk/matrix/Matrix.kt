package io.element.android.x.sdk.matrix

import android.content.Context
import uniffi.matrix_sdk_ffi.AuthenticationService
import java.io.File

class Matrix(
    context: Context,
) {
    private val authFolder = File(context.filesDir, "auth")

    fun login(homeserver: String, username: String, password: String): MatrixClient {
        val authService = AuthenticationService(authFolder.absolutePath)
        authService.configureHomeserver(homeserver)
        val client = authService.login(username, password, "MatrixRustSDKSample", null)
        return MatrixClient(client)
    }
}