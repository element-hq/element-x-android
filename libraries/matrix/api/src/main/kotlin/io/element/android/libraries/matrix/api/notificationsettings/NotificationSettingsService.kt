/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.notificationsettings

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.RoomNotificationSettings
import kotlinx.coroutines.flow.SharedFlow

/**
 * Reads and writes the push rules of the account, which decide what generates a notification.
 *
 * Rules are account data shared across clients, so a change made here is visible on the user's other devices.
 * Several methods take the encryption and one-to-one nature of a room, because Matrix keeps a separate default rule for each combination.
 */
interface NotificationSettingsService {
    /**
     * Emits every time the push rules change, whether from this client or another one, without saying what changed.
     * Callers are expected to re-read the values they display; only the most recent signal is buffered.
     */
    val notificationSettingsChangeFlow: SharedFlow<Unit>

    /**
     * Returns the notification settings that apply to one room, which may come from a room specific rule or from the matching default.
     *
     * @param roomId the room to read the settings of.
     * @param isEncrypted whether that room is encrypted, which selects the relevant default rule.
     * @param isOneToOne whether that room is a direct message, which selects the relevant default rule.
     */
    suspend fun getRoomNotificationSettings(roomId: RoomId, isEncrypted: Boolean, isOneToOne: Boolean): Result<RoomNotificationSettings>

    /**
     * Returns the default notification mode used by rooms that have no rule of their own.
     *
     * @param isEncrypted whether to read the default for encrypted rooms.
     * @param isOneToOne whether to read the default for direct messages.
     */
    suspend fun getDefaultRoomNotificationMode(isEncrypted: Boolean, isOneToOne: Boolean): Result<RoomNotificationMode>

    /**
     * Changes the default notification mode for a whole category of rooms.
     *
     * @param isEncrypted whether to change the default for encrypted rooms.
     * @param mode the new default mode.
     * @param isDM whether to change the default for direct messages.
     */
    suspend fun setDefaultRoomNotificationMode(isEncrypted: Boolean, mode: RoomNotificationMode, isDM: Boolean): Result<Unit>

    /**
     * Sets a room specific notification mode, which from then on overrides the default for that room.
     *
     * @param roomId the room to change.
     * @param mode the new mode for that room.
     */
    suspend fun setRoomNotificationMode(roomId: RoomId, mode: RoomNotificationMode): Result<Unit>

    /**
     * Removes the room specific rule so the room follows the matching default again.
     *
     * @param roomId the room to reset.
     */
    suspend fun restoreDefaultRoomNotificationMode(roomId: RoomId): Result<Unit>

    /**
     * Mutes a room, which is a room specific rule like any other and is undone by [unmuteRoom].
     *
     * @param roomId the room to mute.
     */
    suspend fun muteRoom(roomId: RoomId): Result<Unit>

    /**
     * Unmutes a room, restoring either its previous room specific mode or the matching default.
     *
     * @param roomId the room to unmute.
     * @param isEncrypted whether that room is encrypted, which selects the relevant default rule.
     * @param isOneToOne whether that room is a direct message, which selects the relevant default rule.
     */
    suspend fun unmuteRoom(roomId: RoomId, isEncrypted: Boolean, isOneToOne: Boolean): Result<Unit>

    /** Whether being mentioned through `@room` generates a notification. */
    suspend fun isRoomMentionEnabled(): Result<Boolean>

    /**
     * Sets whether being mentioned through `@room` generates a notification.
     *
     * @param enabled true to be notified of `@room` mentions.
     */
    suspend fun setRoomMentionEnabled(enabled: Boolean): Result<Unit>

    /** Whether incoming calls generate a notification. */
    suspend fun isCallEnabled(): Result<Boolean>

    /**
     * Sets whether incoming calls generate a notification.
     *
     * @param enabled true to be notified of incoming calls.
     */
    suspend fun setCallEnabled(enabled: Boolean): Result<Unit>

    /** Whether room invitations generate a notification. */
    suspend fun isInviteForMeEnabled(): Result<Boolean>

    /**
     * Sets whether room invitations generate a notification.
     *
     * @param enabled true to be notified of invitations.
     */
    suspend fun setInviteForMeEnabled(enabled: Boolean): Result<Unit>

    /** The rooms that have an enabled rule of their own rather than following the defaults. */
    suspend fun getRoomsWithUserDefinedRules(): Result<List<RoomId>>

    /** Whether the homeserver is able to push encrypted events to the device, which decides how much a notification can show before decryption. */
    suspend fun canHomeServerPushEncryptedEventsToDevice(): Result<Boolean>

    /** The push rules as raw JSON, for debugging and bug reports; `null` when the server returned none. */
    suspend fun getRawPushRules(): Result<String?>
}
