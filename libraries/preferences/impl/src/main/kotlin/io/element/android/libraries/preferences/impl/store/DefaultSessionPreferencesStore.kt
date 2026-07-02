/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.impl.store

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.androidutils.hash.hash
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File

class DefaultSessionPreferencesStore(
    context: Context,
    sessionId: SessionId,
    @SessionCoroutineScope sessionCoroutineScope: CoroutineScope,
) : SessionPreferencesStore {
    companion object {
        fun storeFile(context: Context, sessionId: SessionId): File {
            val hashedUserId = sessionId.value.hash().take(16)
            return context.preferencesDataStoreFile("session_${hashedUserId}_preferences")
        }
    }

    private val sharePresenceKey = booleanPreferencesKey("sharePresence")
    private val sendPublicReadReceiptsKey = booleanPreferencesKey("sendPublicReadReceipts")
    private val renderReadReceiptsKey = booleanPreferencesKey("renderReadReceipts")
    private val sendTypingNotificationsKey = booleanPreferencesKey("sendTypingNotifications")
    private val renderTypingNotificationsKey = booleanPreferencesKey("renderTypingNotifications")
    private val skipSessionVerification = booleanPreferencesKey("skipSessionVerification")
    private val compressImages = booleanPreferencesKey("compressMedia")
    private val compressMediaPreset = stringPreferencesKey("compressMediaPreset")

    // Keyed by a hash of the room id rather than declared statically: DataStore doesn't require
    // enumerable keys, and one room can't collide with another's hashed prefix. Also looked up by
    // raw hash, since a channel id enumerated from the system only carries that hash, not the
    // original RoomId.
    private val roomChannelLastNotifiedKeySuffix = "notifChannelLastNotified"
    private fun roomChannelLastNotifiedKey(roomId: RoomId) =
        longPreferencesKey("room_${roomId.value.hash().take(16)}_$roomChannelLastNotifiedKeySuffix")
    private fun roomChannelLastNotifiedKeyFromHash(roomHash: String) =
        longPreferencesKey("room_${roomHash}_$roomChannelLastNotifiedKeySuffix")

    private val dataStoreFile = storeFile(context, sessionId)
    private val store = PreferenceDataStoreFactory.create(
        scope = sessionCoroutineScope,
        migrations = listOf(
            SessionPreferencesStoreMigration(
                sharePresenceKey,
                sendPublicReadReceiptsKey,
            )
        ),
    ) { dataStoreFile }

    override suspend fun setSharePresence(enabled: Boolean) {
        update(sharePresenceKey, enabled)
        // Also update all the other settings
        setSendPublicReadReceipts(enabled)
        setRenderReadReceipts(enabled)
        setSendTypingNotifications(enabled)
        setRenderTypingNotifications(enabled)
    }

    override fun isSharePresenceEnabled(): Flow<Boolean> {
        return get(sharePresenceKey) { true }
    }

    override suspend fun setSendPublicReadReceipts(enabled: Boolean) = update(sendPublicReadReceiptsKey, enabled)
    override fun isSendPublicReadReceiptsEnabled(): Flow<Boolean> = get(sendPublicReadReceiptsKey) { true }

    override suspend fun setRenderReadReceipts(enabled: Boolean) = update(renderReadReceiptsKey, enabled)
    override fun isRenderReadReceiptsEnabled(): Flow<Boolean> = get(renderReadReceiptsKey) { true }

    override suspend fun setSendTypingNotifications(enabled: Boolean) = update(sendTypingNotificationsKey, enabled)
    override fun isSendTypingNotificationsEnabled(): Flow<Boolean> = get(sendTypingNotificationsKey) { true }

    override suspend fun setRenderTypingNotifications(enabled: Boolean) = update(renderTypingNotificationsKey, enabled)
    override fun isRenderTypingNotificationsEnabled(): Flow<Boolean> = get(renderTypingNotificationsKey) { true }

    override suspend fun setSkipSessionVerification(skip: Boolean) = update(skipSessionVerification, skip)
    override fun isSessionVerificationSkipped(): Flow<Boolean> = get(skipSessionVerification) { false }

    override suspend fun setOptimizeImages(compress: Boolean) = update(compressImages, compress)
    override fun doesOptimizeImages(): Flow<Boolean> = get(compressImages) { true }

    override suspend fun setVideoCompressionPreset(preset: VideoCompressionPreset) = update(compressMediaPreset, preset.name)
    override fun getVideoCompressionPreset(): Flow<VideoCompressionPreset> = get(compressMediaPreset) { VideoCompressionPreset.STANDARD.name }
        .map { tryOrNull { VideoCompressionPreset.valueOf(it) } ?: VideoCompressionPreset.STANDARD }

    override suspend fun recordRoomChannelNotified(roomId: RoomId) {
        update(roomChannelLastNotifiedKey(roomId), System.currentTimeMillis())
    }

    override suspend fun clearRoomChannelLastNotified(roomId: RoomId) {
        store.edit { prefs -> prefs.remove(roomChannelLastNotifiedKey(roomId)) }
    }

    override suspend fun getRoomChannelLastNotifiedByHash(): Map<String, Long> {
        val suffix = "_$roomChannelLastNotifiedKeySuffix"
        return store.data.first().asMap().entries
            .mapNotNull { (key, value) ->
                if (key.name.startsWith("room_") && key.name.endsWith(suffix) && value is Long) {
                    key.name.removePrefix("room_").removeSuffix(suffix) to value
                } else {
                    null
                }
            }
            .toMap()
    }

    override suspend fun clearRoomChannelLastNotifiedByHash(roomHash: String) {
        store.edit { prefs -> prefs.remove(roomChannelLastNotifiedKeyFromHash(roomHash)) }
    }

    override suspend fun clear() {
        dataStoreFile.safeDelete()
    }

    private suspend fun <T> update(key: Preferences.Key<T>, value: T) {
        store.edit { prefs -> prefs[key] = value }
    }

    private fun <T> get(key: Preferences.Key<T>, default: () -> T): Flow<T> {
        return store.data.map { prefs -> prefs[key] ?: default() }
    }
}
