/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.channels

import android.app.NotificationChannel
import android.media.AudioAttributes
import android.media.AudioAttributes.USAGE_NOTIFICATION
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.appconfig.NotificationConfig
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.lockscreen.api.LockScreenService
import io.element.android.libraries.androidutils.hash.hash
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import io.element.android.libraries.preferences.api.store.SessionPreferencesStoreFactory
import io.element.android.libraries.push.api.notifications.RoomNotificationChannelManager
import io.element.android.libraries.push.impl.notifications.shortcut.createShortcutId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first

private const val ROOM_NOTIFICATION_CHANNEL_ID_BASE = "ROOM_NOTIFICATION_CHANNEL"

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
private fun supportNotificationChannels() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultRoomNotificationChannelManager(
    private val notificationManager: NotificationManagerCompat,
    private val notificationChannels: NotificationChannels,
    private val enterpriseService: EnterpriseService,
    private val lockScreenService: LockScreenService,
    private val sessionPreferencesStoreFactory: SessionPreferencesStoreFactory,
    @AppCoroutineScope private val appCoroutineScope: CoroutineScope,
) : RoomNotificationChannelManager {
    override suspend fun getChannelIdForRoom(sessionId: SessionId, roomId: RoomId, roomDisplayName: String, isDm: Boolean, noisy: Boolean): String {
        // An MDM-managed channel always wins over a per-room channel, same as
        // NotificationChannels.getChannelIdForMessage: this only ever applies to the noisy path,
        // the silent channel is never enterprise-overridden.
        if (noisy && enterpriseService.getNoisyNotificationChannelId(sessionId) != null) {
            return notificationChannels.getChannelIdForMessage(sessionId, noisy)
        }
        if (!noisy) {
            // See the class doc: only ever promote a room to its own channel from a genuinely
            // noisy notification, so a room whose push rules only bing on mentions doesn't get
            // permanently stuck at whatever importance its first (possibly silent) event implied.
            return notificationChannels.getChannelIdForMessage(sessionId, noisy)
        }
        if (lockScreenService.isPinSetup().first()) {
            // Per-room channels expose the room name and avatar in Android Settings. When app PIN
            // privacy is active, keep using the shared noisy channel just like shortcuts do.
            return notificationChannels.getChannelIdForMessage(sessionId, noisy)
        }
        return ensureRoomChannel(sessionId, roomId, roomDisplayName, isDm)
    }

    override suspend fun clearRoomChannel(sessionId: SessionId, roomId: RoomId) {
        if (!supportNotificationChannels()) return
        notificationManager.deleteNotificationChannel(roomChannelId(sessionId, roomId))
        sessionStore(sessionId).clearRoomChannelLastNotified(roomId)
    }

    override suspend fun pruneChannelsForSession(sessionId: SessionId, roomIds: Set<RoomId>) {
        if (!supportNotificationChannels()) return
        val prefix = roomChannelSessionPrefix(sessionId)
        val validRoomHashes = roomIds.mapTo(mutableSetOf()) { it.value.hash().take(ROOM_HASH_LENGTH) }
        val store = sessionStore(sessionId)
        for (channel in notificationManager.notificationChannels) {
            val id = channel.id
            if (!id.startsWith(prefix)) continue
            val roomHash = id.removePrefix(prefix)
            if (roomHash !in validRoomHashes) {
                notificationManager.deleteNotificationChannel(id)
                store.clearRoomChannelLastNotifiedByHash(roomHash)
            }
        }
        store.getRoomChannelLastNotifiedByHash().keys
            .filter { it !in validRoomHashes }
            .forEach { store.clearRoomChannelLastNotifiedByHash(it) }
    }

    override suspend fun clearAllChannelsForSession(sessionId: SessionId) {
        if (!supportNotificationChannels()) return
        val prefix = roomChannelSessionPrefix(sessionId)
        for (channel in notificationManager.notificationChannels) {
            if (channel.id.startsWith(prefix)) {
                notificationManager.deleteNotificationChannel(channel.id)
                sessionStore(sessionId).clearRoomChannelLastNotifiedByHash(channel.id.removePrefix(prefix))
            }
        }
        val store = sessionStore(sessionId)
        store.getRoomChannelLastNotifiedByHash().keys.forEach { roomHash ->
            store.clearRoomChannelLastNotifiedByHash(roomHash)
        }
    }

    override suspend fun pruneInactiveChannels(sessionId: SessionId) {
        if (!supportNotificationChannels()) return
        val prefix = roomChannelSessionPrefix(sessionId)
        val lastNotifiedByHash = sessionStore(sessionId).getRoomChannelLastNotifiedByHash()
        val now = System.currentTimeMillis()

        val candidates = notificationManager.notificationChannels.mapNotNull { channel ->
            val id = channel.id
            if (!id.startsWith(prefix)) return@mapNotNull null
            if (isProtectedChannel(channel)) return@mapNotNull null
            val roomHash = id.removePrefix(prefix)
            ChannelCandidate(channelId = id, roomHash = roomHash, lastNotifiedAt = lastNotifiedByHash[roomHash] ?: 0L)
        }

        val staleByRetention = candidates.filter { now - it.lastNotifiedAt > CHANNEL_RETENTION_MILLIS }
        val remaining = candidates - staleByRetention.toSet()
        val overBudgetCount = (remaining.size - MAX_CHANNELS).coerceAtLeast(0)
        val oldestOverBudget = remaining.sortedBy { it.lastNotifiedAt }.take(overBudgetCount)

        for (candidate in staleByRetention + oldestOverBudget) {
            notificationManager.deleteNotificationChannel(candidate.channelId)
            sessionStore(sessionId).clearRoomChannelLastNotifiedByHash(candidate.roomHash)
        }
    }

    /**
     * True if [channel]'s live settings no longer match what [ensureRoomChannel] would create, or
     * the user marked it a Priority Conversation - either way, something other than this
     * manager's own creation logic touched it, so [pruneInactiveChannels] must leave it alone.
     */
    private fun isProtectedChannel(channel: NotificationChannel): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && channel.isImportantConversation) return true
        val parentChannel = channel.parentChannelId?.let(notificationManager::getNotificationChannel)
        val expectedSound = if (parentChannel == null) Settings.System.DEFAULT_NOTIFICATION_URI else parentChannel.sound
        return channel.importance != NotificationManagerCompat.IMPORTANCE_DEFAULT ||
            channel.sound != expectedSound ||
            !channel.shouldVibrate() ||
            !channel.shouldShowLights() ||
            channel.lightColor != NotificationConfig.NOTIFICATION_ACCENT_COLOR
    }

    private data class ChannelCandidate(val channelId: String, val roomHash: String, val lastNotifiedAt: Long)

    private fun sessionStore(sessionId: SessionId): SessionPreferencesStore =
        sessionPreferencesStoreFactory.get(sessionId, appCoroutineScope)

    /**
     * Creates the room's channel if it doesn't already exist, records that it was just used (for
     * retention pruning), and returns its id.
     */
    private suspend fun ensureRoomChannel(
        sessionId: SessionId,
        roomId: RoomId,
        roomDisplayName: String,
        isDm: Boolean,
    ): String {
        if (!supportNotificationChannels()) return notificationChannels.getChannelIdForMessage(sessionId, noisy = true)
        val id = roomChannelId(sessionId, roomId)
        if (notificationManager.getNotificationChannel(id) == null) {
            notificationManager.createNotificationChannel(buildRoomChannel(id, sessionId, roomId, roomDisplayName, isDm))
        }
        sessionStore(sessionId).recordRoomChannelNotified(roomId)
        return id
    }

    /**
     * Matches the app's shared noisy channel's defaults (default importance, system default
     * sound, vibration and lights on), since a channel is only ever created when `noisy` is true
     * - see the class doc on [RoomNotificationChannelManager].
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildRoomChannel(
        id: String,
        sessionId: SessionId,
        roomId: RoomId,
        roomDisplayName: String,
        isDm: Boolean,
    ): NotificationChannelCompat {
        val accentColor = NotificationConfig.NOTIFICATION_ACCENT_COLOR
        val parentChannelId = notificationChannels.getChannelIdForMessage(sessionId, noisy = true)
        val parentChannel = notificationManager.getNotificationChannel(parentChannelId)
        val sound = if (parentChannel == null) Settings.System.DEFAULT_NOTIFICATION_URI else parentChannel.sound
        val audioAttributes = parentChannel?.audioAttributes ?: notificationAudioAttributes()
        return NotificationChannelCompat.Builder(id, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(roomDisplayName)
            .setVibrationEnabled(true)
            .setLightsEnabled(true)
            .setLightColor(accentColor)
            .setGroup(if (isDm) PRIVATE_CHATS_CHANNEL_GROUP_ID else ROOMS_CHANNEL_GROUP_ID)
            // Lets Android group this under "Conversations" in system settings and surface the
            // Priority toggle scoped to this one room, rather than the whole shared channel. The
            // conversationId must match the shortcut's id exactly, since that's how system
            // Settings resolves the shortcut (and its icon) for this conversation.
            .setConversationId(parentChannelId, createShortcutId(sessionId, roomId))
            .apply { setRoomChannelSound(sound, audioAttributes) }
            .build()
    }

    private fun NotificationChannelCompat.Builder.setRoomChannelSound(sound: Uri?, audioAttributes: AudioAttributes): NotificationChannelCompat.Builder {
        return if (sound == null) {
            setSound(null, null)
        } else {
            setSound(sound, audioAttributes)
        }
    }

    private fun notificationAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(USAGE_NOTIFICATION)
            .build()
    }

    companion object {
        private const val ROOM_HASH_LENGTH = 16

        /** Retention window for a channel that hasn't notified: 30 days. */
        private const val CHANNEL_RETENTION_MILLIS = 30L * 24 * 60 * 60 * 1000

        /** Soft cap on per-room channels per session; the oldest-notified ones are trimmed above this. */
        private const val MAX_CHANNELS = 50

        private fun roomChannelSessionPrefix(sessionId: SessionId): String =
            "${ROOM_NOTIFICATION_CHANNEL_ID_BASE}_${sessionId.value.hash().take(ROOM_HASH_LENGTH)}_"

        private fun roomChannelId(sessionId: SessionId, roomId: RoomId): String =
            "${roomChannelSessionPrefix(sessionId)}${roomId.value.hash().take(ROOM_HASH_LENGTH)}"
    }
}
