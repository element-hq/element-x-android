/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.coroutine.childScope
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembersState
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.room.draft.ComposerDraft
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPowerLevels
import io.element.android.libraries.matrix.api.roomdirectory.RoomVisibility
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.impl.room.draft.into
import io.element.android.libraries.matrix.impl.room.member.RoomMemberListFetcher
import io.element.android.libraries.matrix.impl.room.member.RoomMemberMapper
import io.element.android.libraries.matrix.impl.room.powerlevels.RoomPowerLevelsMapper
import io.element.android.libraries.matrix.impl.roomdirectory.map
import io.element.android.libraries.matrix.impl.timeline.toRustReceiptType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.use
import timber.log.Timber
import uniffi.matrix_sdk_base.EncryptionState
import org.matrix.rustcomponents.sdk.Room as InnerRoom

class RustBaseRoom(
    override val sessionId: SessionId,
    internal val deviceId: DeviceId,
    internal val innerRoom: InnerRoom,
    coroutineDispatchers: CoroutineDispatchers,
    private val roomSyncSubscriber: RoomSyncSubscriber,
    private val roomMembershipObserver: RoomMembershipObserver,
    sessionCoroutineScope: CoroutineScope,
    initialRoomInfo: RoomInfo,
) : BaseRoom {
    override val roomId = RoomId(innerRoom.id())

    // Create a dispatcher for all room methods...
    private val roomDispatcher = coroutineDispatchers.io.limitedParallelism(32)

    // ...except getMember methods as it could quickly fill the roomDispatcher...
    private val roomMembersDispatcher = coroutineDispatchers.io.limitedParallelism(8)

    internal val roomMemberListFetcher = RoomMemberListFetcher(innerRoom, roomMembersDispatcher)

    override val membersStateFlow: StateFlow<RoomMembersState> = roomMemberListFetcher.membersFlow

    override val roomInfoFlow: StateFlow<RoomInfo> = MutableStateFlow(initialRoomInfo)

    override val roomCoroutineScope = sessionCoroutineScope.childScope(coroutineDispatchers.main, "RoomScope-$roomId")

    override suspend fun subscribeToSync() = roomSyncSubscriber.subscribe(roomId)

    override suspend fun updateMembers() {
        val useCache = membersStateFlow.value is RoomMembersState.Unknown
        val source = if (useCache) {
            RoomMemberListFetcher.Source.CACHE_AND_SERVER
        } else {
            RoomMemberListFetcher.Source.SERVER
        }
        roomMemberListFetcher.fetchRoomMembers(source = source)
    }

    override suspend fun getMembers(limit: Int) = withContext(roomDispatcher) {
        runCatching {
            innerRoom.members().use {
                it.nextChunk(limit.toUInt()).orEmpty().map { roomMember ->
                    RoomMemberMapper.map(roomMember)
                }
            }
        }
    }

    override suspend fun getUpdatedMember(userId: UserId): Result<RoomMember> = withContext(roomDispatcher) {
        runCatching {
            RoomMemberMapper.map(innerRoom.member(userId.value))
        }
    }

    override fun destroy() {
        innerRoom.destroy()
    }

    override suspend fun userDisplayName(userId: UserId): Result<String?> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.memberDisplayName(userId.value)
        }
    }

    override suspend fun userRole(userId: UserId): Result<RoomMember.Role> = withContext(roomDispatcher) {
        runCatching {
            RoomMemberMapper.mapRole(innerRoom.suggestedRoleForUser(userId.value))
        }
    }

    override suspend fun powerLevels(): Result<RoomPowerLevels> = withContext(roomDispatcher) {
        runCatching {
            RoomPowerLevelsMapper.map(innerRoom.getPowerLevels())
        }
    }

    override suspend fun userAvatarUrl(userId: UserId): Result<String?> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.memberAvatarUrl(userId.value)
        }
    }

    override suspend fun leave(): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.leave()
        }.onSuccess {
            roomMembershipObserver.notifyUserLeftRoom(roomId)
        }
    }

    override suspend fun join(): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.join()
        }
    }

    override suspend fun forget(): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.forget()
        }
    }

    override suspend fun canUserInvite(userId: UserId): Result<Boolean> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.canUserInvite(userId.value)
        }
    }

    override suspend fun canUserKick(userId: UserId): Result<Boolean> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.canUserKick(userId.value)
        }
    }

    override suspend fun canUserBan(userId: UserId): Result<Boolean> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.canUserBan(userId.value)
        }
    }

    override suspend fun canUserRedactOwn(userId: UserId): Result<Boolean> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.canUserRedactOwn(userId.value)
        }
    }

    override suspend fun canUserRedactOther(userId: UserId): Result<Boolean> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.canUserRedactOther(userId.value)
        }
    }

    override suspend fun canUserSendState(userId: UserId, type: StateEventType): Result<Boolean> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.canUserSendState(userId.value, type.map())
        }
    }

    override suspend fun canUserSendMessage(userId: UserId, type: MessageEventType): Result<Boolean> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.canUserSendMessage(userId.value, type.map())
        }
    }

    override suspend fun canUserTriggerRoomNotification(userId: UserId): Result<Boolean> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.canUserTriggerRoomNotification(userId.value)
        }
    }

    override suspend fun canUserPinUnpin(userId: UserId): Result<Boolean> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.canUserPinUnpin(userId.value)
        }
    }

    override suspend fun clearEventCacheStorage(): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.clearEventCacheStorage()
        }
    }

    override suspend fun setIsFavorite(isFavorite: Boolean): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.setIsFavourite(isFavorite, null)
        }
    }

    override suspend fun markAsRead(receiptType: ReceiptType): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.markAsRead(receiptType.toRustReceiptType())
        }
    }

    override suspend fun setUnreadFlag(isUnread: Boolean): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.setUnreadFlag(isUnread)
        }
    }

    override suspend fun getPermalink(): Result<String> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.matrixToPermalink()
        }
    }

    override suspend fun getPermalinkFor(eventId: EventId): Result<String> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.matrixToEventPermalink(eventId.value)
        }
    }

    override suspend fun getRoomVisibility(): Result<RoomVisibility> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.getRoomVisibility().map()
        }
    }

    override suspend fun getUpdatedIsEncrypted(): Result<Boolean> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.latestEncryptionState() == EncryptionState.ENCRYPTED
        }
    }

    override suspend fun saveComposerDraft(composerDraft: ComposerDraft): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            Timber.d("saveComposerDraft: $composerDraft into $roomId")
            innerRoom.saveComposerDraft(composerDraft.into())
        }
    }

    override suspend fun loadComposerDraft(): Result<ComposerDraft?> = withContext(roomDispatcher) {
        runCatching {
            Timber.d("loadComposerDraft for $roomId")
            innerRoom.loadComposerDraft()?.into()
        }
    }

    override suspend fun clearComposerDraft(): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            Timber.d("clearComposerDraft for $roomId")
            innerRoom.clearComposerDraft()
        }
    }

    override suspend fun reportRoom(reason: String?): Result<Unit>  = withContext(roomDispatcher) {
        runCatching {
            Timber.d("reportRoom $roomId")
            innerRoom.reportRoom(reason)
        }
    }

}
