/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.channels

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioAttributes.USAGE_NOTIFICATION
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import io.element.android.appconfig.NotificationConfig
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.preferences.api.store.NotificationSound
import io.element.android.libraries.push.api.notifications.NotificationSoundUpdater
import io.element.android.libraries.push.impl.R
import io.element.android.services.toolbox.api.strings.StringProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber

/* ==========================================================================================
 * IDs for channels
 * ========================================================================================== */
internal const val SILENT_NOTIFICATION_CHANNEL_ID = "DEFAULT_SILENT_NOTIFICATION_CHANNEL_ID_V2"
internal const val NOISY_NOTIFICATION_CHANNEL_ID_BASE = "DEFAULT_NOISY_NOTIFICATION_CHANNEL_ID_V2"
internal const val CALL_NOTIFICATION_CHANNEL_ID = "CALL_NOTIFICATION_CHANNEL_ID_V3"
internal const val RINGING_CALL_NOTIFICATION_CHANNEL_ID_BASE = "RINGING_CALL_NOTIFICATION_CHANNEL_ID"

private fun versionedChannelId(base: String, version: Int): String =
    if (version <= 0) base else "${base}_v$version"

internal fun noisyNotificationChannelId(version: Int): String =
    versionedChannelId(NOISY_NOTIFICATION_CHANNEL_ID_BASE, version)

internal fun ringingCallNotificationChannelId(version: Int): String =
    versionedChannelId(RINGING_CALL_NOTIFICATION_CHANNEL_ID_BASE, version)

/**
 * on devices >= android O, we need to define a channel for each notifications.
 *
 * Extends [NotificationSoundUpdater] so consumers can re-create the noisy and ringing-call
 * channels when the user picks a new sound URI in settings.
 */
interface NotificationChannels : NotificationSoundUpdater {
    /**
     * Get the channel for incoming call.
     * @param ring true if the device should ring when receiving the call.
     */
    fun getChannelForIncomingCall(ring: Boolean): String

    /**
     * Get the channel for messages.
     * @param sessionId the session the message belongs to.
     * @param noisy true if the notification should have sound and vibration.
     */
    fun getChannelIdForMessage(sessionId: SessionId, noisy: Boolean): String

    /**
     * Get the channel for test notifications.
     */
    fun getChannelIdForTest(): String

    fun getSilentChannelId(): String = SILENT_NOTIFICATION_CHANNEL_ID
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
private fun supportNotificationChannels() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<NotificationChannels>())
@ContributesBinding(AppScope::class, binding = binding<NotificationSoundUpdater>())
class DefaultNotificationChannels(
    private val notificationManager: NotificationManagerCompat,
    private val stringProvider: StringProvider,
    @ApplicationContext
    private val context: Context,
    private val enterpriseService: EnterpriseService,
    private val appPreferencesStore: AppPreferencesStore,
) : NotificationChannels {
    @Volatile private var currentNoisyChannelId: String = NOISY_NOTIFICATION_CHANNEL_ID_BASE
    @Volatile private var currentRingingCallChannelId: String = RINGING_CALL_NOTIFICATION_CHANNEL_ID_BASE

    // Serializes concurrent recreate* calls; readers stay lock-free via @Volatile on the id fields.
    private val recreateLock = Any()

    init {
        createNotificationChannels()
    }

    /**
     * Create notification channels.
     */
    private fun createNotificationChannels() {
        if (!supportNotificationChannels()) {
            return
        }

        val accentColor = NotificationConfig.NOTIFICATION_ACCENT_COLOR

        // Single-snapshot read; keep this path minimal — extra ContentResolver lookups here would
        // risk a cold-start ANR.
        val config = runBlocking {
            appPreferencesStore.getNotificationSoundChannelConfig()
        }

        currentNoisyChannelId = noisyNotificationChannelId(config.messageSoundVersion)
        currentRingingCallChannelId = ringingCallNotificationChannelId(config.callRingtoneVersion)

        // Migration - the noisy channel was deleted and recreated when sound preference was changed (id was DEFAULT_NOISY_NOTIFICATION_CHANNEL_ID_BASE
        // + currentTimeMillis).
        // Starting from this version the channel will not be dynamic
        for (channel in notificationManager.notificationChannels) {
            val channelId = channel.id
            val legacyBaseName = "DEFAULT_NOISY_NOTIFICATION_CHANNEL_ID_BASE"
            if (channelId.startsWith(legacyBaseName)) {
                notificationManager.deleteNotificationChannel(channelId)
            }
        }
        // Migration - Remove deprecated channels
        for (channelId in listOf(
            "DEFAULT_SILENT_NOTIFICATION_CHANNEL_ID",
            "DEFAULT_NOISY_NOTIFICATION_CHANNEL_ID",
            "CALL_NOTIFICATION_CHANNEL_ID",
            "CALL_NOTIFICATION_CHANNEL_ID_V2",
            "LISTEN_FOR_EVENTS_NOTIFICATION_CHANNEL_ID",
        )) {
            notificationManager.getNotificationChannel(channelId)?.let {
                notificationManager.deleteNotificationChannel(channelId)
            }
        }
        // Drop older versioned channels; only the current one remains.
        deleteStaleVersionedChannels(NOISY_NOTIFICATION_CHANNEL_ID_BASE, currentNoisyChannelId)
        deleteStaleVersionedChannels(RINGING_CALL_NOTIFICATION_CHANNEL_ID_BASE, currentRingingCallChannelId)

        // Default notification importance: shows everywhere, makes noise, but does not visually intrude.
        val noisySoundUri = resolveNoisySoundUri(config.messageSound)
        grantSoundUriToSystem(noisySoundUri)
        notificationManager.createNotificationChannel(
            buildNoisyChannel(
                channelId = currentNoisyChannelId,
                soundUri = noisySoundUri,
                accentColor = accentColor,
            )
        )

        // Low notification importance: shows everywhere, but is not intrusive.
        notificationManager.createNotificationChannel(
            NotificationChannelCompat.Builder(
                SILENT_NOTIFICATION_CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_LOW
            )
                .setName(stringProvider.getString(R.string.notification_channel_silent).ifEmpty { "Silent notifications" })
                .setDescription(stringProvider.getString(R.string.notification_channel_silent))
                .setSound(null, null)
                .setLightsEnabled(true)
                .setLightColor(accentColor)
                .build()
        )

        // Register a channel for incoming and in progress call notifications with no ringing
        notificationManager.createNotificationChannel(
            NotificationChannelCompat.Builder(
                CALL_NOTIFICATION_CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_HIGH
            )
                .setName(stringProvider.getString(R.string.notification_channel_call).ifEmpty { "Call" })
                .setDescription(stringProvider.getString(R.string.notification_channel_call))
                .setVibrationEnabled(true)
                .setLightsEnabled(true)
                .setLightColor(accentColor)
                .build()
        )

        // Register a channel for incoming call notifications which will ring the device when received
        val ringingSoundUri = resolveRingingSoundUri(config.callRingtone)
        grantSoundUriToSystem(ringingSoundUri)
        notificationManager.createNotificationChannel(
            buildRingingCallChannel(
                channelId = currentRingingCallChannelId,
                soundUri = ringingSoundUri,
                accentColor = accentColor,
            )
        )
    }

    private fun buildNoisyChannel(channelId: String, soundUri: Uri?, accentColor: Int): NotificationChannelCompat {
        val builder = NotificationChannelCompat.Builder(channelId, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(stringProvider.getString(R.string.notification_channel_noisy).ifEmpty { "Noisy notifications" })
            .setDescription(stringProvider.getString(R.string.notification_channel_noisy))
            .setVibrationEnabled(true)
            .setLightsEnabled(true)
            .setLightColor(accentColor)
        if (soundUri != null) {
            builder.setSound(
                soundUri,
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(USAGE_NOTIFICATION)
                    .build(),
            )
        } else {
            builder.setSound(null, null)
        }
        return builder.build()
    }

    private fun buildRingingCallChannel(channelId: String, soundUri: Uri?, accentColor: Int): NotificationChannelCompat {
        val builder = NotificationChannelCompat.Builder(channelId, NotificationManagerCompat.IMPORTANCE_MAX)
            .setName(stringProvider.getString(R.string.notification_channel_ringing_calls).ifEmpty { "Ringing calls" })
            .setDescription(stringProvider.getString(R.string.notification_channel_ringing_calls))
            .setVibrationEnabled(true)
            .setLightsEnabled(true)
            .setLightColor(accentColor)
        if (soundUri != null) {
            builder.setSound(
                soundUri,
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setLegacyStreamType(AudioManager.STREAM_RING)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build()
            )
        } else {
            builder.setSound(null, null)
        }
        return builder.build()
    }

    private fun resolveNoisySoundUri(sound: NotificationSound): Uri? = when (sound) {
        NotificationSound.Silent -> null
        NotificationSound.SystemDefault -> bundledMessageSoundUri()
        is NotificationSound.Custom -> parseUriOrFallback(sound.uri) { bundledMessageSoundUri() }
    }

    private fun resolveRingingSoundUri(sound: NotificationSound): Uri? = when (sound) {
        NotificationSound.Silent -> null
        NotificationSound.SystemDefault -> Settings.System.DEFAULT_RINGTONE_URI
        is NotificationSound.Custom -> parseUriOrFallback(sound.uri) { Settings.System.DEFAULT_RINGTONE_URI }
    }

    private fun bundledMessageSoundUri(): Uri =
        "${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.message}".toUri()

    /**
     * Lets system_server ("android") and SystemUI read our FileProvider sound URI; no-op otherwise.
     * SystemUI hosts the lock-screen notification surface on most OEMs, so a missing grant there
     * silently mutes ringtones when the device is locked.
     */
    private fun grantSoundUriToSystem(uri: Uri?) {
        if (uri == null || uri.scheme != ContentResolver.SCHEME_CONTENT) return
        for (pkg in arrayOf("android", "com.android.systemui")) {
            runCatchingExceptions {
                context.grantUriPermission(pkg, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }.onFailure { Timber.w(it, "grantUriPermission(%s) failed for notification sound", pkg) }
        }
    }

    /** Parses [uriString], or returns [fallback] (the SystemDefault URI) on failure. */
    private inline fun parseUriOrFallback(uriString: String, fallback: () -> Uri): Uri =
        runCatchingExceptions { uriString.toUri() }
            .getOrElse {
                Timber.w(it, "Failed to parse persisted sound URI; falling back to default")
                fallback()
            }

    private fun deleteStaleVersionedChannels(baseId: String, currentId: String) {
        if (!supportNotificationChannels()) return
        for (channel in notificationManager.notificationChannels) {
            val id = channel.id
            // Match either the unversioned base ID or any "${baseId}_v<n>" variant.
            val isBaseOrVersioned = id == baseId || id.startsWith("${baseId}_v")
            if (isBaseOrVersioned && id != currentId) {
                notificationManager.deleteNotificationChannel(id)
            }
        }
    }

    override fun getChannelForIncomingCall(ring: Boolean): String {
        return if (ring) currentRingingCallChannelId else CALL_NOTIFICATION_CHANNEL_ID
    }

    override fun getChannelIdForMessage(sessionId: SessionId, noisy: Boolean): String {
        return if (noisy) {
            enterpriseService.getNoisyNotificationChannelId(sessionId)
                ?: currentNoisyChannelId
        } else {
            SILENT_NOTIFICATION_CHANNEL_ID
        }
    }

    override fun getChannelIdForTest(): String = currentNoisyChannelId

    override fun recreateNoisyChannel(sound: NotificationSound, version: Int) {
        if (!supportNotificationChannels()) return
        synchronized(recreateLock) {
            val accentColor = NotificationConfig.NOTIFICATION_ACCENT_COLOR
            val newChannelId = noisyNotificationChannelId(version)
            val soundUri = resolveNoisySoundUri(sound)
            grantSoundUriToSystem(soundUri)
            // Create channel before publishing the id: a reader landing between the assignment and
            // the create call would notify() against a missing id, which Android silently drops.
            notificationManager.createNotificationChannel(
                buildNoisyChannel(newChannelId, soundUri, accentColor)
            )
            currentNoisyChannelId = newChannelId
            deleteStaleVersionedChannels(NOISY_NOTIFICATION_CHANNEL_ID_BASE, newChannelId)
        }
    }

    override fun recreateRingingCallChannel(sound: NotificationSound, version: Int) {
        if (!supportNotificationChannels()) return
        synchronized(recreateLock) {
            val accentColor = NotificationConfig.NOTIFICATION_ACCENT_COLOR
            val newChannelId = ringingCallNotificationChannelId(version)
            val soundUri = resolveRingingSoundUri(sound)
            grantSoundUriToSystem(soundUri)
            // See recreateNoisyChannel: the channel must exist before the id is published.
            notificationManager.createNotificationChannel(
                buildRingingCallChannel(newChannelId, soundUri, accentColor)
            )
            currentRingingCallChannelId = newChannelId
            deleteStaleVersionedChannels(RINGING_CALL_NOTIFICATION_CHANNEL_ID_BASE, newChannelId)
        }
    }

    /**
     * The noisy-channel "default" is our bundled message.mp3, not a system tone — surface it as
     * Custom so the picker row reflects what the user actually hears rather than mislabelling it
     * as Android's system default. Pass null for [ourDefaultUri] to disable the SystemDefault match.
     */
    override suspend fun readNoisyChannelSound(): NotificationSound? {
        return readChannelSound(channelId = currentNoisyChannelId, ourDefaultUri = null)
    }

    override suspend fun readRingingCallChannelSound(): NotificationSound? {
        return readChannelSound(channelId = currentRingingCallChannelId, ourDefaultUri = Settings.System.DEFAULT_RINGTONE_URI)
    }

    /** Classifies the channel's sound URI by comparing against [ourDefaultUri] (null disables the match). */
    private suspend fun readChannelSound(channelId: String, ourDefaultUri: Uri?): NotificationSound? {
        if (!supportNotificationChannels()) return null
        val channel = withContext(Dispatchers.IO) {
            notificationManager.getNotificationChannel(channelId)
        } ?: return null
        return when (val sound = channel.sound) {
            null -> NotificationSound.Silent
            ourDefaultUri -> NotificationSound.SystemDefault
            else -> NotificationSound.Custom(sound.toString())
        }
    }
}
