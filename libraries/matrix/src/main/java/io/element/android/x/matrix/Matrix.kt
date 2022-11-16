package io.element.android.x.matrix

import android.content.Context
import io.element.android.x.core.coroutine.CoroutineDispatchers
import io.element.android.x.matrix.session.SessionStore
import io.element.android.x.matrix.util.logError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.AuthenticationService
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientBuilder
import java.io.File
import java.util.*
import java.util.concurrent.Executors

class Matrix(
    private val coroutineScope: CoroutineScope,
    context: Context,
) {
    private val coroutineDispatchers = CoroutineDispatchers(
        io = Dispatchers.IO,
        computation = Dispatchers.Default,
        main = Dispatchers.Main,
        diffUpdateDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    )
    private val baseFolder = File(context.filesDir, "matrix")
    private val sessionStore = SessionStore(context)
    private val matrixClient = MutableStateFlow<Optional<MatrixClient>>(Optional.empty())
    private val authService = AuthenticationService(baseFolder.absolutePath)

    init {
        sessionStore.isLoggedIn()
            .distinctUntilChanged()
            .onEach { isLoggedIn ->
                if (!isLoggedIn) {
                    matrixClient.value = Optional.empty()
                }
            }
            .launchIn(coroutineScope)
    }

    fun isLoggedIn(): Flow<Boolean> {
        return sessionStore.isLoggedIn()
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

    fun getHomeserver(): String? = authService.homeserverDetails()?.url()

    fun getHomeserverOrDefault(): String = getHomeserver() ?: "matrix.org"

    suspend fun setHomeserver(homeserver: String) {
        withContext(coroutineDispatchers.io) {
            authService.configureHomeserver(homeserver)
        }
    }

    suspend fun login(username: String, password: String): MatrixClient =
        withContext(coroutineDispatchers.io) {
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