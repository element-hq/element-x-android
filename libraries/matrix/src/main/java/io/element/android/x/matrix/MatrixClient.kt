package io.element.android.x.matrix

import io.element.android.x.core.coroutine.CoroutineDispatchers
import io.element.android.x.di.SingleIn
import io.element.android.x.matrix.core.UserId
import io.element.android.x.matrix.media.MediaResolver
import io.element.android.x.matrix.media.RustMediaResolver
import io.element.android.x.matrix.room.MatrixRoom
import io.element.android.x.matrix.room.RoomSummaryDataSource
import io.element.android.x.matrix.room.RustRoomSummaryDataSource
import io.element.android.x.matrix.session.SessionStore
import io.element.android.x.matrix.sync.SlidingSyncObserverProxy
import java.io.Closeable
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientDelegate
import org.matrix.rustcomponents.sdk.MediaSource
import org.matrix.rustcomponents.sdk.RequiredState
import org.matrix.rustcomponents.sdk.SlidingSyncMode
import org.matrix.rustcomponents.sdk.SlidingSyncViewBuilder
import org.matrix.rustcomponents.sdk.StoppableSpawn
import timber.log.Timber

class MatrixClient internal constructor(
    private val client: Client,
    private val sessionStore: SessionStore,
    private val coroutineScope: CoroutineScope,
    private val dispatchers: CoroutineDispatchers,
    private val baseDirectory: File,
) : Closeable {

    val sessionId: String
        get() = "${client.session().userId}_${client.session().deviceId}"

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

    private val slidingSyncView = SlidingSyncViewBuilder()
        .timelineLimit(limit = 10u)
        .requiredState(
            requiredState = listOf(
                RequiredState(key = "m.room.avatar", value = ""),
                RequiredState(key = "m.room.encryption", value = ""),
            )
        )
        .name(name = "HomeScreenView")
        .syncMode(mode = SlidingSyncMode.SELECTIVE)
        .addRange(0u, 30u)
        .build()

    private val slidingSync = client
        .slidingSync()
        .homeserver("https://slidingsync.lab.element.dev")
        .withCommonExtensions()
        // .coldCache("ElementX")
        .addView(slidingSyncView)
        .build()

    private val slidingSyncObserverProxy = SlidingSyncObserverProxy(coroutineScope, dispatchers)
    private val roomSummaryDataSource: RustRoomSummaryDataSource =
        RustRoomSummaryDataSource(
            slidingSyncObserverProxy.updateSummaryFlow,
            slidingSync,
            slidingSyncView,
            dispatchers,
            ::onRestartSync
        )
    private var slidingSyncObserverToken: StoppableSpawn? = null

    private val mediaResolver = RustMediaResolver(this)
    private val isSyncing = AtomicBoolean(false)

    init {
        client.setDelegate(clientDelegate)
    }

    private fun onRestartSync() {
        slidingSyncObserverToken = slidingSync.sync()
    }

    fun getRoom(roomId: String): MatrixRoom? {
        val slidingSyncRoom = slidingSync.getRoom(roomId) ?: return null
        val room = slidingSyncRoom.fullRoom() ?: return null
        return MatrixRoom(
            slidingSyncUpdateFlow = slidingSyncObserverProxy.updateSummaryFlow,
            slidingSyncRoom = slidingSyncRoom,
            room = room,
            coroutineScope = coroutineScope,
            coroutineDispatchers = dispatchers
        )
    }

    fun startSync() {
        if (isSyncing.compareAndSet(false, true)) {
            roomSummaryDataSource.startSync()
            slidingSync.setObserver(slidingSyncObserverProxy)
            slidingSyncObserverToken = slidingSync.sync()
        }
    }

    fun stopSync() {
        if (isSyncing.compareAndSet(true, false)) {
            roomSummaryDataSource.stopSync()
            slidingSync.setObserver(null)
            slidingSyncObserverToken?.cancel()
        }
    }

    fun roomSummaryDataSource(): RoomSummaryDataSource = roomSummaryDataSource

    fun mediaResolver(): MediaResolver = mediaResolver

    override fun close() {
        stopSync()
        roomSummaryDataSource.close()
        client.setDelegate(null)
    }

    suspend fun logout() = withContext(dispatchers.io) {
        close()
        try {
            client.logout()
        } catch (failure: Throwable) {
            Timber.e(failure, "Fail to call logout on HS. Still delete local files.")
        }
        baseDirectory.deleteSessionDirectory(userID = client.userId())
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

    @OptIn(ExperimentalUnsignedTypes::class)
    suspend fun loadMediaContentForSource(source: MediaSource): Result<ByteArray> =
        withContext(dispatchers.io) {
            runCatching {
                client.getMediaContent(source).toUByteArray().toByteArray()
            }
        }

    @OptIn(ExperimentalUnsignedTypes::class)
    suspend fun loadMediaThumbnailForSource(
        source: MediaSource,
        width: Long,
        height: Long
    ): Result<ByteArray> =
        withContext(dispatchers.io) {
            runCatching {
                client.getMediaThumbnail(source, width.toULong(), height.toULong()).toUByteArray()
                    .toByteArray()
            }
        }

    private fun File.deleteSessionDirectory(userID: String): Boolean {
        // Rust sanitises the user ID replacing invalid characters with an _
        val sanitisedUserID = userID.replace(":", "_")
        val sessionDirectory = File(this, sanitisedUserID)
        return sessionDirectory.deleteRecursively()
    }
}
