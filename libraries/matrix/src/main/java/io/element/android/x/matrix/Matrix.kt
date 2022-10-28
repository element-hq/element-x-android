package io.element.android.x.matrix

import android.content.Context
import io.element.android.x.core.data.CoroutineDispatchers
import io.element.android.x.matrix.session.SessionStore
import io.element.android.x.matrix.util.logError
import kotlinx.coroutines.Dispatchers
import org.matrix.rustcomponents.sdk.AuthenticationService
import org.matrix.rustcomponents.sdk.ClientBuilder
import java.io.File

class Matrix(
    context: Context,
) {

    private val coroutineDispatchers = CoroutineDispatchers(
        io = Dispatchers.IO,
        computation = Dispatchers.Default,
        main = Dispatchers.Main
    )
    private val baseFolder = File(context.filesDir, "matrix")
    private val sessionStore = SessionStore(context)

    suspend fun restoreSession(): MatrixClient? {
        return sessionStore.getStoredData()
            ?.let { sessionData ->
                try {
                    ClientBuilder()
                        .basePath(baseFolder.absolutePath)
                        .username(sessionData.userId)
                        .build().apply {
                            restoreLogin(sessionData.restoreToken)
                        }
                } catch (throwable: Throwable) {
                    logError(throwable)
                    null
                }
            }?.let {
                MatrixClient(it, sessionStore, coroutineDispatchers)
            }
    }

    suspend fun login(homeserver: String, username: String, password: String): MatrixClient {
        val authService = AuthenticationService(baseFolder.absolutePath)
        authService.configureHomeserver(homeserver)
        val client = authService.login(username, password, "MatrixRustSDKSample", null)
        sessionStore.storeData(SessionStore.SessionData(client.userId(), client.restoreToken()))
        return MatrixClient(client, sessionStore, coroutineDispatchers)
    }
}