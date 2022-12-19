package io.element.android.x.matrix

import android.content.Context
import coil.ComponentRegistry
import io.element.android.x.core.coroutine.CoroutineDispatchers
import io.element.android.x.di.AppScope
import io.element.android.x.di.ApplicationContext
import io.element.android.x.di.SingleIn
import io.element.android.x.matrix.media.MediaFetcher
import io.element.android.x.matrix.media.MediaKeyer
import io.element.android.x.matrix.session.SessionStore
import io.element.android.x.matrix.util.logError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.AuthenticationService
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientBuilder
import timber.log.Timber
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import javax.inject.Inject

@SingleIn(AppScope::class)
class Matrix @Inject constructor(
    private val coroutineScope: CoroutineScope,
    @ApplicationContext context: Context,
) {
    private val coroutineDispatchers = CoroutineDispatchers(
        io = Dispatchers.IO,
        computation = Dispatchers.Default,
        main = Dispatchers.Main,
        diffUpdateDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    )
    private val baseDirectory = File(context.filesDir, "sessions")
    private val sessionStore = SessionStore(context)
    private val authService = AuthenticationService(baseDirectory.absolutePath)

    fun isLoggedIn(): Flow<Boolean> {
        return sessionStore.isLoggedIn()
    }

    fun registerCoilComponents(
        builder: ComponentRegistry.Builder,
        activeClientProvider: () -> MatrixClient?
    ) {
        builder.add(MediaKeyer())
        builder.add(MediaFetcher.Factory(activeClientProvider))
    }

    suspend fun restoreSession() = withContext(coroutineDispatchers.io) {
        sessionStore.getLatestSession()
            ?.let { session ->
                try {
                    ClientBuilder()
                        .basePath(baseDirectory.absolutePath)
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
            val client = try {
                authService.login(username, password, "ElementX Android", null)
            } catch (failure: Throwable) {
                Timber.e(failure, "Fail login")
                throw failure
            }
            sessionStore.storeData(client.session())
            createMatrixClient(client)
        }

    private fun createMatrixClient(client: Client): MatrixClient {
        return MatrixClient(
            client = client,
            sessionStore = sessionStore,
            coroutineScope = coroutineScope,
            dispatchers = coroutineDispatchers,
            baseDirectory = baseDirectory,
        )
    }
}
