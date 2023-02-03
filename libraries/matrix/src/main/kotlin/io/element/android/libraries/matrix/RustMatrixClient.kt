/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.matrix

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.core.RoomId
import io.element.android.libraries.matrix.core.SessionId
import io.element.android.libraries.matrix.core.UserId
import io.element.android.libraries.matrix.media.MediaResolver
import io.element.android.libraries.matrix.media.RustMediaResolver
import io.element.android.libraries.matrix.room.MatrixRoom
import io.element.android.libraries.matrix.room.RoomSummaryDataSource
import io.element.android.libraries.matrix.room.RustMatrixRoom
import io.element.android.libraries.matrix.room.RustRoomSummaryDataSource
import io.element.android.libraries.matrix.session.SessionStore
import io.element.android.libraries.matrix.session.sessionId
import io.element.android.libraries.matrix.sync.SlidingSyncObserverProxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientDelegate
import org.matrix.rustcomponents.sdk.MediaSource
import org.matrix.rustcomponents.sdk.RequiredState
import org.matrix.rustcomponents.sdk.SlidingSyncMode
import org.matrix.rustcomponents.sdk.SlidingSyncRequestListFilters
import org.matrix.rustcomponents.sdk.SlidingSyncViewBuilder
import org.matrix.rustcomponents.sdk.StoppableSpawn
import timber.log.Timber
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

internal class RustMatrixClient internal constructor(
    private val client: Client,
    private val sessionStore: SessionStore,
    private val coroutineScope: CoroutineScope,
    private val dispatchers: CoroutineDispatchers,
    private val baseDirectory: File,
) : MatrixClient {

    override val sessionId: SessionId = client.session().sessionId()

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

    private val slidingSyncFilters by lazy {
        SlidingSyncRequestListFilters(
            isDm = null,
            spaces = emptyList(),
            isEncrypted = null,
            isInvite = false,
            isTombstoned = false,
            roomTypes = emptyList(),
            notRoomTypes = listOf("m.space"),
            roomNameLike = null,
            tags = emptyList(),
            notTags = emptyList()
        )
    }

    private val visibleRoomsView = SlidingSyncViewBuilder()
        .timelineLimit(limit = 10u)
        .requiredState(
            requiredState = listOf(
                RequiredState(key = "m.room.avatar", value = ""),
                RequiredState(key = "m.room.encryption", value = ""),
            )
        )
        .filters(slidingSyncFilters)
        .name(name = "CurrentlyVisibleRooms")
        .sendUpdatesForItems(true)
        .syncMode(mode = SlidingSyncMode.SELECTIVE)
        .addRange(0u, 20u)
        .build()

    private val slidingSync = client
        .slidingSync()
        .homeserver("https://slidingsync.lab.matrix.org")
        .withCommonExtensions()
        .coldCache("ElementX")
        .addView(visibleRoomsView)
        .build()

    private val slidingSyncObserverProxy = SlidingSyncObserverProxy(coroutineScope)
    private val roomSummaryDataSource: RustRoomSummaryDataSource =
        RustRoomSummaryDataSource(
            slidingSyncObserverProxy.updateSummaryFlow,
            slidingSync,
            visibleRoomsView,
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

    override fun getRoom(roomId: RoomId): MatrixRoom? {
        val slidingSyncRoom = slidingSync.getRoom(roomId.value) ?: return null
        val room = slidingSyncRoom.fullRoom() ?: return null
        return RustMatrixRoom(
            slidingSyncUpdateFlow = slidingSyncObserverProxy.updateSummaryFlow,
            slidingSyncRoom = slidingSyncRoom,
            room = room,
            coroutineScope = coroutineScope,
            coroutineDispatchers = dispatchers
        )
    }

    override fun startSync() {
        if (isSyncing.compareAndSet(false, true)) {
            roomSummaryDataSource.startSync()
            slidingSync.setObserver(slidingSyncObserverProxy)
            slidingSyncObserverToken = slidingSync.sync()
        }
    }

    override fun stopSync() {
        if (isSyncing.compareAndSet(true, false)) {
            roomSummaryDataSource.stopSync()
            slidingSync.setObserver(null)
            slidingSyncObserverToken?.cancel()
        }
    }

    override fun roomSummaryDataSource(): RoomSummaryDataSource = roomSummaryDataSource

    override fun mediaResolver(): MediaResolver = mediaResolver

    override fun close() {
        stopSync()
        roomSummaryDataSource.close()
        client.setDelegate(null)
    }

    override suspend fun logout() = withContext(dispatchers.io) {
        close()
        try {
            client.logout()
        } catch (failure: Throwable) {
            Timber.e(failure, "Fail to call logout on HS. Still delete local files.")
        }
        baseDirectory.deleteSessionDirectory(userID = client.userId())
        sessionStore.reset()
    }

    override fun userId(): UserId = UserId(client.userId())

    override suspend fun loadUserDisplayName(): Result<String> = withContext(dispatchers.io) {
        runCatching {
            client.displayName()
        }
    }

    override suspend fun loadUserAvatarURLString(): Result<String> = withContext(dispatchers.io) {
        runCatching {
            client.avatarUrl()
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun loadMediaContentForSource(source: MediaSource): Result<ByteArray> =
        withContext(dispatchers.io) {
            runCatching {
                client.getMediaContent(source).toUByteArray().toByteArray()
            }
        }

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun loadMediaThumbnailForSource(
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
