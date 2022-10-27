package io.element.android.x.sdk.matrix

import android.content.Context
import io.element.android.x.sdk.matrix.store.SessionStore
import io.element.android.x.sdk.matrix.util.logError
import org.matrix.rustcomponents.sdk.AuthenticationService
import org.matrix.rustcomponents.sdk.ClientBuilder
import java.io.File

class Matrix(
    context: Context,
) {
    private val baseFolder = File(context.filesDir, "matrix")
    private val sessionStore = SessionStore(context)

    suspend fun restoreSession(): MatrixClient? {
        return sessionStore.getStoredData()
            ?.let { sessionData ->
                try {
                    val client = ClientBuilder()
                        .basePath(baseFolder.absolutePath)
                        .username(sessionData.userId)
                        .build()
                    client.restoreLogin(sessionData.restoreToken)
                    client
                } catch (throwable: Throwable) {
                    logError(throwable)
                    null
                }
            }?.let {
                MatrixClient(it, sessionStore)
            }
    }

    suspend fun login(homeserver: String, username: String, password: String): MatrixClient {
        val authService = AuthenticationService(baseFolder.absolutePath)
        authService.configureHomeserver(homeserver)
        val client = authService.login(username, password, "MatrixRustSDKSample", null)
        sessionStore.storeData(SessionStore.SessionData(client.userId(), client.restoreToken()))
        return MatrixClient(client, sessionStore)
    }
}