package io.element.android.x.matrix

import io.element.android.x.core.data.CoroutineDispatchers
import io.element.android.x.matrix.core.UserId
import io.element.android.x.matrix.room.RoomSummaryDataSource
import io.element.android.x.matrix.room.RustRoomSummaryDataSource
import io.element.android.x.matrix.session.SessionStore
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.*
import timber.log.Timber
import java.io.Closeable

class MatrixClient internal constructor(
    private val client: Client,
    private val sessionStore: SessionStore,
    private val dispatchers: CoroutineDispatchers,
) : Closeable {

    private val clientDelegate = object : ClientDelegate {
        override fun didReceiveAuthError(isSoftLogout: Boolean) {
            Timber.v("didReceiveAuthError()")
        }

        override fun didReceiveSyncUpdate() {
            Timber.v("didReceiveSyncUpdate()")
        }

        override fun didUpdateRestoreToken() {
            Timber.v("didUpdateRestoreToken()")
        }
    }

    private val slidingSyncObserver = object : SlidingSyncObserver {
        override fun didReceiveSyncUpdate(summary: UpdateSummary) {
            Timber.v("didReceiveSyncUpdate=$summary on Thread: ${Thread.currentThread()}")
            roomSummaryDataSource.updateRoomsWithIdentifiers(summary.rooms)
        }
    }

    private val slidingSyncView = SlidingSyncViewBuilder()
        .timelineLimit(limit = 1u)
        .requiredState(requiredState = listOf(RequiredState(key = "m.room.avatar", value = "")))
        .name(name = "HomeScreenView")
        .syncMode(mode = SlidingSyncMode.FULL_SYNC)
        .build()

    private val slidingSync = client
        .slidingSync()
        .homeserver("https://slidingsync.lab.element.dev")
        .addView(slidingSyncView)
        .build()

    private val roomSummaryDataSource: RustRoomSummaryDataSource =
        RustRoomSummaryDataSource(slidingSync, slidingSyncView, dispatchers)
    private var slidingSyncObserverToken: StoppableSpawn? = null

    init {
        client.setDelegate(clientDelegate)
    }

    fun startSync() {
        slidingSync.setObserver(slidingSyncObserver)
        slidingSyncObserverToken = slidingSync.sync()
    }

    fun stopSync() {
        roomSummaryDataSource.stopSync()
        slidingSync.setObserver(null)
        slidingSyncObserverToken?.cancel()
    }

    fun roomSummaryDataSource(): RoomSummaryDataSource = roomSummaryDataSource

    override fun close() {
        stopSync()
        roomSummaryDataSource.close()
        client.setDelegate(null)
    }

    suspend fun logout() = withContext(dispatchers.io) {
        close()
        client.logout()
        sessionStore.reset()
    }

    fun userId(): UserId = UserId(client.userId())
    suspend fun loadUserDisplayName(): Result<String> = withContext(dispatchers.io) {
        runCatching {
            client.displayName()
        }
    }

    suspend fun loadUserAvatarURLString(): Result<String> = withContext(dispatchers.io) {
        runCatching {
            client.avatarUrl()
        }
    }

    suspend fun loadMediaContentForSource(source: MediaSource): Result<List<UByte>> =
        withContext(dispatchers.io) {
            runCatching {
                client.getMediaContent(source)
            }
        }


}
