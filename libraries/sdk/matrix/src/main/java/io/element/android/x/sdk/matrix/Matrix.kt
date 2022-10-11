package io.element.android.x.sdk.matrix

import android.content.Context
import io.element.android.x.sdk.matrix.store.SessionStore
import org.matrix.rustcomponents.sdk.AuthenticationService
import org.matrix.rustcomponents.sdk.ClientBuilder
import java.io.File

class Matrix(
    context: Context,
) {
    private val authFolder = File(context.filesDir, "auth")
    private val sessionStore = SessionStore(context)

    suspend fun restoreSession(): MatrixClient? {
        return sessionStore.getStoredData()
            ?.let { sessionData ->
                val client = ClientBuilder()
                    .username(sessionData.userId)
                    .build()
                client.restoreLogin(sessionData.restoreToken)
                client
            }?.let {
                MatrixClient(it)
            }
    }

    suspend fun login(homeserver: String, username: String, password: String): MatrixClient {
        val authService = AuthenticationService(authFolder.absolutePath)
        authService.configureHomeserver(homeserver)
        val client = authService.login(username, password, "MatrixRustSDKSample", null)
        sessionStore.storeData(SessionStore.SessionData(username, client.restoreToken()))
        return MatrixClient(client)
    }
}