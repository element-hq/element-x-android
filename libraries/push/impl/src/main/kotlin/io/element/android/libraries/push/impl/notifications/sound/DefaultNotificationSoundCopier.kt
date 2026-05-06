/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.sound

import android.content.ContentResolver
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.push.api.notifications.sound.NotificationSoundCopier
import io.element.android.libraries.push.api.notifications.sound.NotificationSoundCopier.CopyResult
import io.element.android.libraries.push.api.notifications.sound.NotificationSoundCopier.SoundSlot
import io.element.android.libraries.push.impl.notifications.notificationSoundFileProviderAuthority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.io.InputStream

private const val MAX_BYTES = 5L * 1024 * 1024
private const val DIRECTORY_NAME = "notification_sounds"
private const val FALLBACK_EXTENSION = "bin"
private const val COPY_BUFFER_SIZE = 8 * 1024
private const val MAX_DISPLAY_NAME_LENGTH = 256

// Defence-in-depth: we only ever expect URIs from the system ringtone picker (content://) or
// the bundled resource (android.resource://). Reject anything else before doing I/O so a future
// caller that constructs a NotificationSound.Custom from an untrusted intent can't trick the
// copier into reading file:// or unrelated providers.
private val ALLOWED_SOURCE_SCHEMES = setOf(
    ContentResolver.SCHEME_CONTENT,
    ContentResolver.SCHEME_ANDROID_RESOURCE,
)

@ContributesBinding(AppScope::class)
class DefaultNotificationSoundCopier(
    @ApplicationContext private val context: Context,
) : NotificationSoundCopier {
    // Per-slot serialization: concurrent picks for the same slot would race on `<slot>.tmp`.
    private val slotMutexes = SoundSlot.entries.associateWith { Mutex() }

    override suspend fun copyToAppFiles(sourceUriString: String, slot: SoundSlot): CopyResult = withContext(Dispatchers.IO) {
        slotMutexes.getValue(slot).withLock {
            runCatchingExceptions { performCopy(sourceUriString, slot) }
                .getOrElse { ex ->
                    // Don't pass the throwable to Timber: SAF/MediaProvider exceptions sometimes
                    // embed the source URI (which can carry short-lived auth tokens).
                    Timber.w("Notification sound copy failed for slot=%s cause=%s", slot, ex::class.simpleName)
                    CopyResult.Failure(ex)
                }
        }
    }

    override suspend fun deleteStoredSoundFor(slot: SoundSlot) {
        withContext(Dispatchers.IO) {
            slotMutexes.getValue(slot).withLock {
                val dir = File(context.filesDir, DIRECTORY_NAME)
                if (!dir.exists()) return@withLock
                val slotBase = slotBaseFor(slot)
                dir.listFiles { f -> f.isFile && f.name.startsWith("$slotBase.") }
                    ?.forEach { stale ->
                        if (!stale.delete()) {
                            Timber.w("Could not remove stored slot file %s", stale.name)
                        }
                    }
            }
        }
    }

    private fun slotBaseFor(slot: SoundSlot): String = when (slot) {
        SoundSlot.Message -> "message_sound"
        SoundSlot.Call -> "call_sound"
    }

    private fun performCopy(sourceUriString: String, slot: SoundSlot): CopyResult {
        val source = sourceUriString.toUri()
        if (source.scheme !in ALLOWED_SOURCE_SCHEMES) return CopyResult.UnplayableSource
        // Reject unplayable sources up front instead of persisting a known-broken pick.
        val sourceRingtone = RingtoneManager.getRingtone(context, source)
            ?: return CopyResult.UnplayableSource
        // Capture the source's display name before copy — once persisted, the row only sees the
        // FileProvider URI, where Ringtone.getTitle() would resolve to our internal filename
        // (e.g. "call_sound.ogg"). RingtoneManager → MediaStore TITLE → OpenableColumns covers
        // system tones, MediaStore-indexed audio, and SAF-picked files respectively. The value
        // crosses our security boundary (foreign provider → persisted state → UI), so cap and
        // sanitize it.
        val displayName = (sourceRingtone.getTitle(context)?.takeUnless { it.isBlank() }
            ?: queryOpenableDisplayName(source))
            ?.let(::sanitizeDisplayName)

        // MIME comes from an arbitrary content provider; refuse extensions outside the platform
        // table to keep filenames bounded to notification_sounds/.
        val mimeMap = MimeTypeMap.getSingleton()
        val candidateExtension = mimeMap.getExtensionFromMimeType(context.contentResolver.getType(source))
        val extension = candidateExtension
            ?.takeIf { it.isNotBlank() && mimeMap.hasExtension(it) }
            ?: FALLBACK_EXTENSION

        val slotBase = slotBaseFor(slot)

        val dir = File(context.filesDir, DIRECTORY_NAME).apply { mkdirs() }
        val tmpFile = File(dir, "$slotBase.tmp").also { if (it.exists()) it.delete() }
        val finalFile = File(dir, "$slotBase.$extension")

        val inputStream = context.contentResolver.openInputStream(source)
            ?: return CopyResult.UnplayableSource
        val bytesCopied = try {
            inputStream.use { input -> tmpFile.outputStream().buffered().use { out -> input.copyToCapped(out, MAX_BYTES) } }
        } catch (_: SizeLimitExceeded) {
            tmpFile.delete()
            Timber.w("Notification sound copy aborted: source exceeds %d bytes (slot=%s)", MAX_BYTES, slot)
            return CopyResult.FileTooLarge
        }

        // The finally below guarantees the temp file doesn't outlive a thrown probe / rename.
        var committed = false
        try {
            // Probe through the FileProvider URI we'll actually persist (catches torn writes
            // and OEM codec quirks).
            val fileProviderUri = FileProvider.getUriForFile(
                context,
                context.notificationSoundFileProviderAuthority(),
                tmpFile,
            )
            if (RingtoneManager.getRingtone(context, fileProviderUri) == null) {
                return CopyResult.UnplayableCopy
            }

            // Sweep all `<slot>.*` so an extension change between picks doesn't leak orphans.
            dir.listFiles { f -> f.isFile && f.name.startsWith("$slotBase.") && f.name != tmpFile.name }
                ?.forEach { stale ->
                    if (!stale.delete()) {
                        Timber.w("Could not remove stale slot file %s", stale.name)
                    }
                }
            if (!tmpFile.renameTo(finalFile)) {
                return CopyResult.Failure(IOException("Could not rename temp file to ${finalFile.name}"))
            }
            committed = true

            val finalUri = FileProvider.getUriForFile(
                context,
                context.notificationSoundFileProviderAuthority(),
                finalFile,
            )
            Timber.d("Notification sound copied: slot=%s bytes=%d", slot, bytesCopied)
            return CopyResult.Success(finalUri.toString(), displayName)
        } finally {
            if (!committed) tmpFile.delete()
        }
    }

    private fun queryOpenableDisplayName(uri: Uri): String? {
        return runCatchingExceptions {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst() && cursor.columnCount > 0) {
                        cursor.getString(0)?.takeUnless { it.isBlank() }
                    } else {
                        null
                    }
                }
        }.getOrNull()
    }
}

internal fun sanitizeDisplayName(raw: String): String? {
    // Display name comes from a foreign content provider; strip C0 / DEL control characters and
    // cap length so a hostile or malformed value can't bloat persisted state or break UI rendering.
    val cleaned = buildString(raw.length.coerceAtMost(MAX_DISPLAY_NAME_LENGTH)) {
        for (ch in raw) {
            if (length >= MAX_DISPLAY_NAME_LENGTH) break
            if (ch.code >= 0x20 && ch.code != 0x7F) append(ch)
        }
    }.trim()
    return cleaned.takeUnless { it.isEmpty() }
}

private class SizeLimitExceeded : IOException("Sound source exceeds the configured size limit")

private fun InputStream.copyToCapped(out: java.io.OutputStream, maxBytes: Long): Long {
    val buffer = ByteArray(COPY_BUFFER_SIZE)
    var totalBytes = 0L
    while (true) {
        val read = read(buffer)
        if (read == -1) break
        totalBytes += read
        if (totalBytes > maxBytes) throw SizeLimitExceeded()
        out.write(buffer, 0, read)
    }
    out.flush()
    return totalBytes
}
