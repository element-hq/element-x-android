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
import io.element.android.features.announcement.api.Announcement
import io.element.android.features.announcement.api.AnnouncementService
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.preferences.api.store.NotificationSound
import io.element.android.libraries.preferences.api.store.NotificationSoundChannelConfig
import io.element.android.libraries.preferences.api.store.NotificationSoundUnavailableState
import io.element.android.libraries.push.api.notifications.NotificationSoundUpdater
import io.element.android.libraries.push.api.notifications.SoundDisplayNameResolver
import io.element.android.libraries.push.impl.R
import io.element.android.services.toolbox.api.strings.StringProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    private val soundDisplayNameResolver: SoundDisplayNameResolver,
    private val announcementService: AnnouncementService,
    @AppCoroutineScope
    private val appCoroutineScope: CoroutineScope,
) : NotificationChannels {
    @Volatile private var currentNoisyChannelId: String = NOISY_NOTIFICATION_CHANNEL_ID_BASE
    @Volatile private var currentRingingCallChannelId: String = RINGING_CALL_NOTIFICATION_CHANNEL_ID_BASE

    // Serializes recreate* against itself so concurrent sound-picker changes can't interleave the
    // currentChannelId write with the createNotificationChannel + deleteStaleVersionedChannels calls.
    // Readers stay lock-free thanks to @Volatile on the id fields.
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

        // Read the user's persisted sound preferences synchronously so the channels created on
        // app boot match what the user has chosen. A single DataStore read pulls all four values
        // out of the same Preferences snapshot. Probing custom URIs for resolvability is moved
        // off this thread (see [scheduleSanitizeUnavailableSounds] below) — that path can do
        // multiple ContentResolver lookups and would risk an ANR on cold start.
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
        // Clean up stale versioned channels (any older version of the noisy or ringing-call channels
        // that the user no longer has selected). Leaves only the current version.
        deleteStaleVersionedChannels(NOISY_NOTIFICATION_CHANNEL_ID_BASE, currentNoisyChannelId)
        deleteStaleVersionedChannels(RINGING_CALL_NOTIFICATION_CHANNEL_ID_BASE, currentRingingCallChannelId)

        // Default notification importance: shows everywhere, makes noise, but does not visually intrude.
        notificationManager.createNotificationChannel(
            buildNoisyChannel(
                channelId = currentNoisyChannelId,
                soundUri = resolveNoisySoundUri(config.messageSound),
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
        notificationManager.createNotificationChannel(
            buildRingingCallChannel(
                channelId = currentRingingCallChannelId,
                soundUri = resolveRingingSoundUri(config.callRingtone),
                accentColor = accentColor,
            )
        )

        // Probe persisted Custom URIs for resolvability off the calling thread. Unresolvable
        // sounds are reverted to SystemDefault and a one-time banner is shown via the home
        // screen.
        scheduleSanitizeUnavailableSounds(config)
    }

    private fun scheduleSanitizeUnavailableSounds(config: NotificationSoundChannelConfig) {
        appCoroutineScope.launch {
            sanitizeUnavailableSounds(config)
        }
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
     * Parses [uriString] into a [Uri], or returns the [fallback] on failure. Note that for both
     * the noisy and ringing-call channels, the fallback is the **same** URI that [resolveNoisySoundUri]
     * / [resolveRingingSoundUri] would return for [NotificationSound.SystemDefault] — so a malformed
     * persisted URI lands the user in the same audible state as if they had picked SystemDefault.
     * The display name layer agrees because [SoundDisplayNameResolver.resolveCustomSoundTitle] also
     * returns null for the same input, falling through to the default label. Sanitization on the
     * next launch promotes the persisted state from `Custom("…")` back to [NotificationSound.SystemDefault].
     */
    private inline fun parseUriOrFallback(uriString: String, fallback: () -> Uri): Uri =
        runCatchingExceptions { uriString.toUri() }
            .getOrElse {
                Timber.w(it, "Failed to parse persisted sound URI; falling back to default")
                fallback()
            }

    /**
     * Probes each Custom URI via [SoundDisplayNameResolver] (the same path used by the settings
     * screen). If the underlying ringtone is no longer resolvable — file deleted, app uninstalled,
     * permission revoked — reverts the affected sound to [NotificationSound.SystemDefault] (which
     * also recreates the channel with the bumped version) and posts a one-time banner via
     * [AnnouncementService]. The persisted [NotificationSoundUnavailableState] is read by the
     * home screen to render the appropriate variant of the banner.
     *
     * Ordering: URI resets and channel recreation happen FIRST. If they throw, the persisted
     * unavailable-state stays at its prior value — next boot will re-detect and retry. The state
     * write and the announcement are at the tail so the user never sees a banner pointing at a
     * URI we failed to actually reset. The full body is wrapped in [runCatchingExceptions] so any
     * failure is logged and retried on the next launch instead of crashing app start.
     */
    internal suspend fun sanitizeUnavailableSounds(config: NotificationSoundChannelConfig) {
        runCatchingExceptions {
            val messageUnavailable = (config.messageSound as? NotificationSound.Custom)?.let {
                soundDisplayNameResolver.resolveCustomSoundTitle(it.uri) == null
            } ?: false
            val callUnavailable = (config.callRingtone as? NotificationSound.Custom)?.let {
                soundDisplayNameResolver.resolveCustomSoundTitle(it.uri) == null
            } ?: false

            val state = NotificationSoundUnavailableState.from(messageUnavailable, callUnavailable)
            if (state == NotificationSoundUnavailableState.None) {
                return@runCatchingExceptions
            }

            Timber.w(
                "Detected unavailable notification sound(s); reverting to default. " +
                    "message=$messageUnavailable call=$callUnavailable"
            )

            if (messageUnavailable) {
                val newVersion = appPreferencesStore.setMessageSoundAndIncrementVersion(NotificationSound.SystemDefault)
                recreateNoisyChannel(NotificationSound.SystemDefault, newVersion)
            }
            if (callUnavailable) {
                val newVersion = appPreferencesStore.setCallRingtoneAndIncrementVersion(NotificationSound.SystemDefault)
                recreateRingingCallChannel(NotificationSound.SystemDefault, newVersion)
            }

            // Single atomic write — the banner can never observe a half-set state.
            appPreferencesStore.setNotificationSoundUnavailableState(state)
            announcementService.showAnnouncement(Announcement.SoundUnavailable)
        }.onFailure {
            Timber.w(it, "Failed to sanitize unavailable notification sounds; will retry on next launch")
        }
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
            // Create the channel BEFORE publishing the new id to lock-free readers. If the
            // assignment landed first, a reader between the two lines would receive an id for a
            // channel that does not yet exist on NotificationManager — and on Android O+ a notify()
            // against a missing channel id is silently dropped, losing the notification.
            notificationManager.createNotificationChannel(
                buildNoisyChannel(newChannelId, resolveNoisySoundUri(sound), accentColor)
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
            // See recreateNoisyChannel: the channel must exist before the id is published.
            notificationManager.createNotificationChannel(
                buildRingingCallChannel(newChannelId, resolveRingingSoundUri(sound), accentColor)
            )
            currentRingingCallChannelId = newChannelId
            deleteStaleVersionedChannels(RINGING_CALL_NOTIFICATION_CHANNEL_ID_BASE, newChannelId)
        }
    }
}
