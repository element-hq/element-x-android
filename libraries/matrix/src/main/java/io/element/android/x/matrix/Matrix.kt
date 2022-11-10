package io.element.android.x.matrix

import android.content.Context
import io.element.android.x.core.coroutine.CoroutineDispatchers
import io.element.android.x.matrix.session.SessionStore
import io.element.android.x.matrix.util.logError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.AuthenticationService
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientBuilder
import java.io.File
import java.util.*

class Matrix(
    private val coroutineScope: CoroutineScope,
    context: Context,
) {
    private val coroutineDispatchers = CoroutineDispatchers(
        io = Dispatchers.IO,
        computation = Dispatchers.Default,
        main = Dispatchers.Main
    )
    private val baseFolder = File(context.filesDir, "matrix")
    private val sessionStore = SessionStore(context)
    private val matrixClient = MutableStateFlow<Optional<MatrixClient>>(Optional.empty())
    private val isLoggedIn = MutableStateFlow(false)

    init {
        sessionStore.isLoggedIn()
            .distinctUntilChanged()
            .onEach { isLoggedIn ->
                this.isLoggedIn.value = isLoggedIn
                if (!isLoggedIn) {
                    matrixClient.value = Optional.empty()
                }
            }
            .launchIn(coroutineScope)
    }

    fun isLoggedIn(): Flow<Boolean> {
        return isLoggedIn
    }

    fun client(): Flow<Optional<MatrixClient>> {
        return matrixClient
    }

    fun activeClient(): MatrixClient {
        return matrixClient.value.get()
    }

    suspend fun restoreSession() = withContext(coroutineDispatchers.io) {
        sessionStore.getLatestSession()
            ?.let { session ->
                try {
                    ClientBuilder()
                        .basePath(baseFolder.absolutePath)
                        .username(session.userId)
                        .build().apply {
                            restoreSession(session)
                        }
                } catch (throwable: Throwable) {
                    logError(throwable)
                    null
                }
            }?.let {
                createMatrixClient(it)
            }
    }

    suspend fun login(homeserver: String, username: String, password: String): MatrixClient =
        withContext(coroutineDispatchers.io) {
            val authService = AuthenticationService(baseFolder.absolutePath)
            authService.configureHomeserver(homeserver)
            val client = authService.login(username, password, "MatrixRustSDKSample", null)
            sessionStore.storeData(client.session())
            createMatrixClient(client)
        }

    private fun createMatrixClient(client: Client): MatrixClient {
        return MatrixClient(
            client = client,
            sessionStore = sessionStore,
            coroutineScope = coroutineScope,
            dispatchers = coroutineDispatchers
        ).also {
            matrixClient.value = Optional.of(it)
        }
    }
}