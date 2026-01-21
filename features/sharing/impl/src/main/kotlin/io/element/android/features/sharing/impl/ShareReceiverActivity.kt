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
import androidx.activity.ComponentActivity
import androidx.core.content.pm.ShortcutManagerCompat
import io.element.android.features.share.api.ShareEntryPoint


class ShareReceiverActivity : ComponentActivity() {
    companion object {
        private const val PREFS_NAME = "sharing_shortcuts_prefs"
        private const val PREF_PREFIX = "shareshortcut.room."


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
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val roomIdFromExtra = incoming?.getStringExtra("room_id")
        val resolvedRoomId = when {
            !shortcutId.isNullOrEmpty() -> {
                prefs.getString(PREF_PREFIX + shortcutId, null) ?: roomIdFromExtra
            }
            else -> roomIdFromExtra
        }

        if (resolvedRoomId.isNullOrEmpty()) {
            val text = incoming?.getStringExtra(Intent.EXTRA_TEXT)
            val uris = extractUris(incoming)
            launchMainActivity(null, text, uris, incoming?.type)
            finish()
            return
        }


        when (action) {
            Intent.ACTION_SEND -> {
                if (type?.startsWith("text") == true) {
                    handleSendText(incoming, resolvedRoomId)
                } else {
                    handleSendStream(incoming, resolvedRoomId)
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                handleSendMultipleStreams(incoming, resolvedRoomId)
            }
            else -> {
                val text = incoming?.getStringExtra(Intent.EXTRA_TEXT)
                val uris = extractUris(incoming)
                launchMainActivity(null, text, uris, incoming?.type)
            }
        }

        shortcutId?.let { ShortcutManagerCompat.reportShortcutUsed(this, it) }

        finish()
    }

    private fun handleSendText(intent: Intent, roomId: String) {
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
        launchMainActivity(roomId, text, null, intent.type)
    }

    private fun handleSendStream(intent: Intent, roomId: String) {
        val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        val uris = if (uri != null) arrayListOf(uri) else null
        takePersistablePermissionsIfNeeded(uris)
        launchMainActivity(roomId, null, uris, intent.type)
    }

    private fun handleSendMultipleStreams(intent: Intent, roomId: String) {
        val uris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
        takePersistablePermissionsIfNeeded(uris)
        launchMainActivity(roomId, null, uris, intent.type)
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

    private fun launchMainActivity(roomId: String?, text: String?, uris: ArrayList<Uri>?, type: String?) {
        val out = Intent(this, Class.forName("io.element.android.x.MainActivity")).apply {
            action = Intent.ACTION_SEND
            this.type = type
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (roomId != null) {
                putExtra(ShareEntryPoint.EXTRA_SHARE_TARGET_ROOM_ID, roomId)
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
