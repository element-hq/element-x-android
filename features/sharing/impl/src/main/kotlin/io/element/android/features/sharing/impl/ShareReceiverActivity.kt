/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.sharing.impl

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.core.content.pm.ShortcutManagerCompat
import io.element.android.features.share.api.ShareEntryPoint
import io.element.android.features.sharing.api.SharingConstants


class ShareReceiverActivity : ComponentActivity() {
    companion object {
        const val EXTRA_TARGET_ROOM_ID = "io.element.android.features.sharing.extra.TARGET_ROOM_ID"
        const val EXTRA_SHARED_TEXT = "io.element.android.features.sharing.extra.SHARED_TEXT"
        const val EXTRA_SHARED_URIS = "io.element.android.features.sharing.extra.SHARED_URIS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val incoming = intent
        val action = incoming?.action
        val type = incoming?.type

        val shortcutId = incoming?.getStringExtra(Intent.EXTRA_SHORTCUT_ID)
        val (resolvedSessionId, resolvedRoomId) = if (shortcutId?.startsWith("directshare_") == true) {
            try {
                val parts = shortcutId.split("_")
                val sessionEncoded = parts[1]
                val roomEncoded = parts[2]
                val sessionDecoded = String(Base64.decode(sessionEncoded, Base64.NO_WRAP))
                val roomDecoded = String(Base64.decode(roomEncoded, Base64.NO_WRAP))
                sessionDecoded to roomDecoded
            } catch (e: Exception) {
                null to null
            }
        } else {
            null to null
        }.let { (sessionFromId, roomFromId) ->
            (sessionFromId ?: incoming?.getStringExtra(SharingConstants.EXTRA_SHARE_TARGET_SESSION_ID)) to
                (roomFromId ?: incoming?.getStringExtra(SharingConstants.EXTRA_SHARE_TARGET_ROOM_ID))
        }

        if (resolvedRoomId.isNullOrEmpty()) {
            val text = incoming?.getStringExtra(Intent.EXTRA_TEXT)
            val uris = extractUris(incoming)
            launchMainActivity(null, null, text, uris, incoming?.type)
            finish()
            return
        }


        when (action) {
            Intent.ACTION_SEND -> {
                if (type?.startsWith("text") == true) {
                    handleSendText(incoming, resolvedRoomId, resolvedSessionId)
                } else {
                    handleSendStream(incoming, resolvedRoomId, resolvedSessionId)
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                handleSendMultipleStreams(incoming, resolvedRoomId, resolvedSessionId)
            }
            else -> {
                val text = incoming?.getStringExtra(Intent.EXTRA_TEXT)
                val uris = extractUris(incoming)
                launchMainActivity(null, null, text, uris, incoming?.type)
            }
        }

        shortcutId?.let { ShortcutManagerCompat.reportShortcutUsed(this, it) }

        finish()
    }

    private fun handleSendText(intent: Intent, roomId: String, sessionId: String?) {
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
        launchMainActivity(roomId, sessionId, text, null, intent.type)
    }

    private fun handleSendStream(intent: Intent, roomId: String, sessionId: String?) {
        val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        val uris = if (uri != null) arrayListOf(uri) else null
        takePersistablePermissionsIfNeeded(uris)
        launchMainActivity(roomId, sessionId, null, uris, intent.type)
    }

    private fun handleSendMultipleStreams(intent: Intent, roomId: String, sessionId: String?) {
        val uris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
        takePersistablePermissionsIfNeeded(uris)
        launchMainActivity(roomId, sessionId, null, uris, intent.type)
    }

    private fun extractUris(intent: Intent?): ArrayList<Uri>? {
        if (intent == null) return null
        return when (intent.action) {
            Intent.ACTION_SEND -> {
                intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { arrayListOf(it) }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
            }
            else -> null
        }
    }

    private fun takePersistablePermissionsIfNeeded(uris: List<Uri>?) {
        try {
            val flags = intent.flags
            if (uris != null && flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0) {
                for (uri in uris) {
                    try {
                        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    } catch (e: SecurityException) {
                    }
                }
            }
        } catch (t: Throwable) {
        }
    }

    private fun launchMainActivity(roomId: String?, sessionId: String?, text: String?, uris: ArrayList<Uri>?, type: String?) {
        val out = Intent(this, Class.forName("io.element.android.x.MainActivity")).apply {
            action = Intent.ACTION_SEND
            this.type = type
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (roomId != null) {
                putExtra(ShareEntryPoint.EXTRA_SHARE_TARGET_ROOM_ID, roomId)
            }
            if (sessionId != null) {
                putExtra(SharingConstants.EXTRA_SHARE_TARGET_SESSION_ID, sessionId)
            }
            if (!text.isNullOrEmpty()) putExtra(Intent.EXTRA_TEXT, text)
            if (!uris.isNullOrEmpty()) {
                if (uris.size == 1) {
                    putExtra(Intent.EXTRA_STREAM, uris.first())
                } else {
                    action = Intent.ACTION_SEND_MULTIPLE
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                clipData = intent.clipData
            }
        }
        startActivity(out)
    }
}
