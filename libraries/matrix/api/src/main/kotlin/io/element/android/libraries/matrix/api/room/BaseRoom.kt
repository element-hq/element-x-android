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
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.draft.ComposerDraft
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPowerLevels
import io.element.android.libraries.matrix.api.room.tombstone.PredecessorRoom
import io.element.android.libraries.matrix.api.roomdirectory.RoomVisibility
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import java.io.Closeable

/**
 * This interface represents the common functionality for a local room, whether it's joined, invited, knocked, or left.
 */
interface BaseRoom : Closeable {
    /**
     * The session id of the current user.
     */
    val sessionId: SessionId

    /**
     * The id of the room.
     */
    val roomId: RoomId

    /**
     * The coroutine scope that will handle all jobs related to this room.
     */
    val roomCoroutineScope: CoroutineScope

    /**
     * The current loaded members as a StateFlow.
     * Initial value is [RoomMembersState.Unknown].
     * To update them you should call [updateMembers].
     */
    val membersStateFlow: StateFlow<RoomMembersState>

    /**
     * A flow that emits the current [RoomInfo] state.
     */
    val roomInfoFlow: StateFlow<RoomInfo>

    /**
     * Get the latest room info we have received from the SDK stream.
     */
    fun info(): RoomInfo = roomInfoFlow.value

    fun predecessorRoom(): PredecessorRoom?

    /**
     * A one-to-one is a room with exactly 2 members.
     * See [the Matrix spec](https://spec.matrix.org/latest/client-server-api/#default-underride-rules).
     */
    val isOneToOne: Boolean get() = info().activeMembersCount == 2L

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

    /**
     * Adds the room to the sync subscription list.
     */
    suspend fun subscribeToSync()

    /**
     * Gets the power levels of the room.
     */
    suspend fun powerLevels(): Result<RoomPowerLevels>

    /**
     * Gets the role of the user with the provided [userId] in the room.
     */
    suspend fun userRole(userId: UserId): Result<RoomMember.Role>

    /**
     * Gets the display name of the user with the provided [userId] in the room.
     */
    suspend fun userDisplayName(userId: UserId): Result<String?>

    /**
     * Gets the avatar of the user with the provided [userId] in the room.
     */
    suspend fun userAvatarUrl(userId: UserId): Result<String?>

    /**
     * Leaves and forgets the room. Only joined, invited or knocked rooms can be left.
     */
    suspend fun leave(): Result<Unit>

    /**
     * Joins the room. Only invited rooms can be joined.
     */
    suspend fun join(): Result<Unit>

    /**
     * Forgets about the room, removing it from the server and the local cache. Only left and banned rooms can be forgotten.
     */
    suspend fun forget(): Result<Unit>

    /**
     * Returns `true` if the user with the provided [userId] can invite other users to the room.
     */
    suspend fun canUserInvite(userId: UserId): Result<Boolean>

    /**
     * Returns `true` if the user with the provided [userId] can kick other users from the room.
     */
    suspend fun canUserKick(userId: UserId): Result<Boolean>

    /**
     * Returns `true` if the user with the provided [userId] can ban other users from the room.
     */
    suspend fun canUserBan(userId: UserId): Result<Boolean>

    /**
     * Returns `true` if the user with the provided [userId] can redact their own messages.
     */
    suspend fun canUserRedactOwn(userId: UserId): Result<Boolean>

    /**
     * Returns `true` if the user with the provided [userId] can redact messages from other users.
     */
    suspend fun canUserRedactOther(userId: UserId): Result<Boolean>

    /**
     * Returns `true` if the user with the provided [userId] can send state events.
     */
    suspend fun canUserSendState(userId: UserId, type: StateEventType): Result<Boolean>

    /**
     * Returns `true` if the user with the provided [userId] can send messages.
     */
    suspend fun canUserSendMessage(userId: UserId, type: MessageEventType): Result<Boolean>

    /**
     * Returns `true` if the user with the provided [userId] can trigger an `@room` notification.
     */
    suspend fun canUserTriggerRoomNotification(userId: UserId): Result<Boolean>

    /**
     * Returns `true` if the user with the provided [userId] can pin or unpin messages.
     */
    suspend fun canUserPinUnpin(userId: UserId): Result<Boolean>

    /**
     * Returns `true` if the user with the provided [userId] can join or starts calls.
     */
    suspend fun canUserJoinCall(userId: UserId): Result<Boolean> =
        canUserSendState(userId, StateEventType.CALL_MEMBER)

    /**
     * Sets the room as favorite or not, based on the [isFavorite] parameter.
     */
    suspend fun setIsFavorite(isFavorite: Boolean): Result<Unit>

    /**
     * Mark the room as read by trying to attach an unthreaded read receipt to the latest room event.
     * @param receiptType The type of receipt to send.
     */
    suspend fun markAsRead(receiptType: ReceiptType): Result<Unit>

    /**
     * Sets a flag on the room to indicate that the user has explicitly marked it as unread, or reverts the flag.
     * @param isUnread true to mark the room as unread, false to remove the flag.
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

    /**
     * Returns the visibility for this room in the room directory, fetching it from the homeserver if needed.
     */
    suspend fun getUpdatedIsEncrypted(): Result<Boolean>

    /**
     * Store the given `ComposerDraft` in the state store of this room.
     */
    suspend fun saveComposerDraft(composerDraft: ComposerDraft, threadRoot: ThreadId?): Result<Unit>

    /**
     * Retrieve the `ComposerDraft` stored in the state store for this room.
     */
    suspend fun loadComposerDraft(threadRoot: ThreadId?): Result<ComposerDraft?>

    /**
     * Clear the `ComposerDraft` stored in the state store for this room.
     */
    suspend fun clearComposerDraft(threadRoot: ThreadId?): Result<Unit>

    /**
     * Reports a room as inappropriate to the server.
     * The caller is not required to be joined to the room to report it.
     * @param reason - The reason the room is being reported.
     */
    suspend fun reportRoom(reason: String?): Result<Unit>

    /**
     * Destroy the room and release all resources associated to it.
     */
    fun destroy()

    override fun close() = destroy()
}
