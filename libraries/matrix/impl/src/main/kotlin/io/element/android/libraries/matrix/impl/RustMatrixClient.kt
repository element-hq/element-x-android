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

package io.element.android.libraries.matrix.impl

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.impl.media.RustMediaResolver
import io.element.android.libraries.matrix.impl.room.RustMatrixRoom
import io.element.android.libraries.matrix.impl.room.RustRoomSummaryDataSource
import io.element.android.libraries.matrix.impl.sync.SlidingSyncObserverProxy
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.matrix.api.media.MediaResolver
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomSummaryDataSource
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.impl.verification.MatrixSessionVerificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientDelegate
import org.matrix.rustcomponents.sdk.MediaSource
import org.matrix.rustcomponents.sdk.RequiredState
import org.matrix.rustcomponents.sdk.SessionVerificationController
import org.matrix.rustcomponents.sdk.SlidingSyncMode
import org.matrix.rustcomponents.sdk.SlidingSyncRequestListFilters
import org.matrix.rustcomponents.sdk.SlidingSyncViewBuilder
import org.matrix.rustcomponents.sdk.TaskHandle
import timber.log.Timber
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class RustMatrixClient constructor(
    private val client: Client,
    private val sessionStore: SessionStore,
    private val coroutineScope: CoroutineScope,
    private val dispatchers: CoroutineDispatchers,
    private val baseDirectory: File,
) : MatrixClient {

    override val sessionId: UserId = UserId(client.userId())

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
        .timelineLimit(limit = 1u)
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
    private var slidingSyncObserverToken: TaskHandle? = null

    private val sessionVerificationService = MatrixSessionVerificationService(client.getSessionVerificationController())

    private val mediaResolver = RustMediaResolver(this)
    private val isSyncing = AtomicBoolean(false)

    init {
        client.setDelegate(clientDelegate)
        roomSummaryDataSource.init()
        slidingSync.setObserver(slidingSyncObserverProxy)
    }

    private fun onRestartSync() {
        stopSync()
        startSync()
    }

    override fun getRoom(roomId: RoomId): MatrixRoom? {
        val slidingSyncRoom = slidingSync.getRoom(roomId.value) ?: return null
        val fullRoom = slidingSyncRoom.fullRoom() ?: return null
        return RustMatrixRoom(
            slidingSyncUpdateFlow = slidingSyncObserverProxy.updateSummaryFlow,
            slidingSyncRoom = slidingSyncRoom,
            innerRoom = fullRoom,
            coroutineScope = coroutineScope,
            coroutineDispatchers = dispatchers
        )
    }

    override fun roomSummaryDataSource(): RoomSummaryDataSource = roomSummaryDataSource

    override fun mediaResolver(): MediaResolver = mediaResolver

    override fun sessionVerificationService(): SessionVerificationService = sessionVerificationService

    override fun startSync() {
        if (client.isSoftLogout()) return
        if (isSyncing.compareAndSet(false, true)) {
            slidingSyncObserverToken = slidingSync.sync()
        }
    }

    override fun stopSync() {
        if (isSyncing.compareAndSet(true, false)) {
            slidingSyncObserverToken?.cancel()
        }
    }

    override fun close() {
        stopSync()
        slidingSync.setObserver(null)
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
        sessionStore.removeSession(client.userId())
    }

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
