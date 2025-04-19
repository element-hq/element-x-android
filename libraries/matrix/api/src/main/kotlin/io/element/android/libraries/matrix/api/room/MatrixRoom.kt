/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.draft.ComposerDraft
import io.element.android.libraries.matrix.api.room.powerlevels.MatrixRoomPowerLevels
import io.element.android.libraries.matrix.api.roomdirectory.RoomVisibility
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import java.io.Closeable

interface MatrixRoom : Closeable {
    val sessionId: SessionId
    val roomId: RoomId

    val roomCoroutineScope: CoroutineScope

    /**
     * The current loaded members as a StateFlow.
     * Initial value is [MatrixRoomMembersState.Unknown].
     * To update them you should call [updateMembers].
     */
    val membersStateFlow: StateFlow<MatrixRoomMembersState>

    val roomInfoFlow: StateFlow<MatrixRoomInfo>

    /**
     * Get the latest room info we have received from the SDK stream.
     */
    fun info(): MatrixRoomInfo = roomInfoFlow.value

    /**
     * Try to load the room members and update the membersFlow.
     */
    suspend fun updateMembers()

    /**
     * Get the members of the room. Note: generally this should not be used, please use
     * [membersStateFlow] and [updateMembers] instead.
     */
    suspend fun getMembers(limit: Int = 5): Result<List<RoomMember>>

    /**
     * Will return an updated member or an error.
     */
    suspend fun getUpdatedMember(userId: UserId): Result<RoomMember>

    fun destroy()

    suspend fun subscribeToSync()

    suspend fun powerLevels(): Result<MatrixRoomPowerLevels>

    suspend fun userRole(userId: UserId): Result<RoomMember.Role>

    suspend fun userDisplayName(userId: UserId): Result<String?>

    suspend fun userAvatarUrl(userId: UserId): Result<String?>

    suspend fun leave(): Result<Unit>

    suspend fun join(): Result<Unit>

    suspend fun forget(): Result<Unit>

    suspend fun canUserInvite(userId: UserId): Result<Boolean>

    suspend fun canUserKick(userId: UserId): Result<Boolean>

    suspend fun canUserBan(userId: UserId): Result<Boolean>

    suspend fun canUserRedactOwn(userId: UserId): Result<Boolean>

    suspend fun canUserRedactOther(userId: UserId): Result<Boolean>

    suspend fun canUserSendState(userId: UserId, type: StateEventType): Result<Boolean>

    suspend fun canUserSendMessage(userId: UserId, type: MessageEventType): Result<Boolean>

    suspend fun canUserTriggerRoomNotification(userId: UserId): Result<Boolean>

    suspend fun canUserPinUnpin(userId: UserId): Result<Boolean>

    suspend fun canUserJoinCall(userId: UserId): Result<Boolean> =
        canUserSendState(userId, StateEventType.CALL_MEMBER)

    suspend fun setIsFavorite(isFavorite: Boolean): Result<Unit>

    /**
     * Mark the room as read by trying to attach an unthreaded read receipt to the latest room event.
     * @param receiptType The type of receipt to send.
     */
    suspend fun markAsRead(receiptType: ReceiptType): Result<Unit>

    /**
     * Sets a flag on the room to indicate that the user has explicitly marked it as unread, or reverts the flag.
     * @param isUnread true to mark the room as unread, false to remove the flag.
     *
     */
    suspend fun setUnreadFlag(isUnread: Boolean): Result<Unit>

    /**
     * Clear the event cache storage for the current room.
     */
    suspend fun clearEventCacheStorage(): Result<Unit>

    /**
     * Get the permalink for the room.
     */
    suspend fun getPermalink(): Result<String>

    /**
     * Get the permalink for the provided [eventId].
     * @param eventId The event id to get the permalink for.
     * @return The permalink, or a failure.
     */
    suspend fun getPermalinkFor(eventId: EventId): Result<String>

    /**
     * Returns the visibility for this room in the room directory.
     * If the room is not published, the result will be [RoomVisibility.Private].
     */
    suspend fun getRoomVisibility(): Result<RoomVisibility>

    suspend fun getUpdatedIsEncrypted(): Result<Boolean>

    /**
     * Store the given `ComposerDraft` in the state store of this room.
     */
    suspend fun saveComposerDraft(composerDraft: ComposerDraft): Result<Unit>

    /**
     * Retrieve the `ComposerDraft` stored in the state store for this room.
     */
    suspend fun loadComposerDraft(): Result<ComposerDraft?>

    /**
     * Clear the `ComposerDraft` stored in the state store for this room.
     */
    suspend fun clearComposerDraft(): Result<Unit>

    override fun close() = destroy()
}
