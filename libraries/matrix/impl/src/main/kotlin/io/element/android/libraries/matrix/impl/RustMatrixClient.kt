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
import io.element.android.libraries.matrix.api.media.MediaResolver
import io.element.android.libraries.matrix.api.notification.NotificationService
import io.element.android.libraries.matrix.api.pusher.PushersService
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.room.RoomSummaryDataSource
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.impl.media.RustMediaResolver
import io.element.android.libraries.matrix.impl.notification.RustNotificationService
import io.element.android.libraries.matrix.impl.pushers.RustPushersService
import io.element.android.libraries.matrix.impl.room.RustMatrixRoom
import io.element.android.libraries.matrix.impl.room.RustRoomSummaryDataSource
import io.element.android.libraries.matrix.impl.sync.SlidingSyncObserverProxy
import io.element.android.libraries.matrix.impl.verification.RustSessionVerificationService
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientDelegate
import org.matrix.rustcomponents.sdk.RequiredState
import org.matrix.rustcomponents.sdk.SlidingSyncListBuilder
import org.matrix.rustcomponents.sdk.SlidingSyncMode
import org.matrix.rustcomponents.sdk.SlidingSyncRequestListFilters
import org.matrix.rustcomponents.sdk.TaskHandle
import org.matrix.rustcomponents.sdk.mediaSourceFromUrl
import org.matrix.rustcomponents.sdk.use
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

    private val verificationService = RustSessionVerificationService()
    private val pushersService = RustPushersService(
        client = client,
        dispatchers = dispatchers,
    )
    private val notificationService = RustNotificationService(baseDirectory)
    private var slidingSyncUpdateJob: Job? = null

    private val clientDelegate = object : ClientDelegate {
        override fun didReceiveAuthError(isSoftLogout: Boolean) {
            //TODO handle this
            Timber.v("didReceiveAuthError(isSoftLogout=$isSoftLogout)")
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

    private val visibleRoomsSlidingSyncList = SlidingSyncListBuilder()
        .timelineLimit(limit = 1u)
        .requiredState(
            requiredState = listOf(
                RequiredState(key = "m.room.avatar", value = ""),
                RequiredState(key = "m.room.encryption", value = ""),
                RequiredState(key = "m.room.join_rules", value = ""),
            )
        )
        .filters(slidingSyncFilters)
        .name(name = "CurrentlyVisibleRooms")
        .sendUpdatesForItems(true)
        .syncMode(mode = SlidingSyncMode.SELECTIVE)
        .addRange(0u, 20u)
        .use {
            it.build()
        }

    private val slidingSync = client
        .slidingSync()
        .homeserver("https://slidingsync.lab.matrix.org")
        .withCommonExtensions()
        .coldCache("ElementX")
        .addList(visibleRoomsSlidingSyncList)
        .use {
            it.build()
        }

    private val slidingSyncObserverProxy = SlidingSyncObserverProxy(coroutineScope)
    private val rustRoomSummaryDataSource: RustRoomSummaryDataSource =
        RustRoomSummaryDataSource(
            slidingSyncObserverProxy.updateSummaryFlow,
            slidingSync,
            visibleRoomsSlidingSyncList,
            dispatchers,
            ::onRestartSync
        )

    override val roomSummaryDataSource: RoomSummaryDataSource
        get() = rustRoomSummaryDataSource

    private var slidingSyncObserverToken: TaskHandle? = null

    private val mediaResolver = RustMediaResolver(this)
    private val isSyncing = AtomicBoolean(false)

    private val roomMembershipObserver = RoomMembershipObserver(sessionId)

    init {
        client.setDelegate(clientDelegate)
        rustRoomSummaryDataSource.init()
        slidingSync.setObserver(slidingSyncObserverProxy)
        slidingSyncUpdateJob = slidingSyncObserverProxy.updateSummaryFlow
            .onEach { onSlidingSyncUpdate() }
            .launchIn(coroutineScope)
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
            coroutineDispatchers = dispatchers,
        )
    }

    override fun mediaResolver(): MediaResolver = mediaResolver

    override fun sessionVerificationService(): SessionVerificationService = verificationService

    override fun pushersService(): PushersService = pushersService

    override fun notificationService(): NotificationService = notificationService

    override fun startSync() {
        if (isSyncing.compareAndSet(false, true)) {
            slidingSyncObserverToken = slidingSync.sync()
        }
    }

    override fun stopSync() {
        if (isSyncing.compareAndSet(true, false)) {
            slidingSyncObserverToken?.use { it.cancel() }
        }
    }

    override fun close() {
        slidingSyncUpdateJob?.cancel()
        stopSync()
        slidingSync.setObserver(null)
        rustRoomSummaryDataSource.close()
        client.setDelegate(null)
        visibleRoomsSlidingSyncList.destroy()
        slidingSync.destroy()
        verificationService.destroy()
        client.destroy()
    }

    override suspend fun logout() = withContext(dispatchers.io) {
        try {
            client.logout()
        } catch (failure: Throwable) {
            Timber.e(failure, "Fail to call logout on HS. Still delete local files.")
        }
        baseDirectory.deleteSessionDirectory(userID = client.userId())
        sessionStore.removeSession(client.userId())
        close()
    }

    override suspend fun loadUserDisplayName(): Result<String> = withContext(dispatchers.io) {
        runCatching {
            client.displayName()
        }
    }

    override suspend fun loadUserAvatarURLString(): Result<String?> = withContext(dispatchers.io) {
        runCatching {
            client.avatarUrl()
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun loadMediaContent(url: String): Result<ByteArray> =
        withContext(dispatchers.io) {
            runCatching {
                mediaSourceFromUrl(url).use { source ->
                    client.getMediaContent(source).toUByteArray().toByteArray()
                }
            }
        }

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun loadMediaThumbnail(
        url: String,
        width: Long,
        height: Long
    ): Result<ByteArray> =
        withContext(dispatchers.io) {
            runCatching {
                mediaSourceFromUrl(url).use { mediaSource ->
                    client.getMediaThumbnail(
                        mediaSource = mediaSource,
                        width = width.toULong(),
                        height = height.toULong()
                    ).toUByteArray().toByteArray()
                }
            }
        }

    override fun onSlidingSyncUpdate() {
        if (!verificationService.isReady.value) {
            try {
                verificationService.verificationController = client.getSessionVerificationController()
            } catch (e: Throwable) {
                Timber.e(e, "Could not start verification service. Will try again on the next sliding sync update.")
            }
        }
    }

    override fun roomMembershipObserver(): RoomMembershipObserver = roomMembershipObserver

    private fun File.deleteSessionDirectory(userID: String): Boolean {
        // Rust sanitises the user ID replacing invalid characters with an _
        val sanitisedUserID = userID.replace(":", "_")
        val sessionDirectory = File(this, sanitisedUserID)
        return sessionDirectory.deleteRecursively()
    }
}
