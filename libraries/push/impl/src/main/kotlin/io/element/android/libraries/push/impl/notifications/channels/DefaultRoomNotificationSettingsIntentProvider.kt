/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.channels

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.notifications.RoomNotificationChannelManager
import io.element.android.libraries.push.api.notifications.RoomNotificationSettingsIntentProvider
import io.element.android.libraries.push.impl.notifications.shortcut.createShortcutId

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultRoomNotificationSettingsIntentProvider(
    @ApplicationContext private val context: Context,
    private val roomNotificationChannelManager: RoomNotificationChannelManager,
) : RoomNotificationSettingsIntentProvider {
    override suspend fun getIntent(
        sessionId: SessionId,
        roomId: RoomId,
        roomDisplayName: String,
        isDm: Boolean,
    ): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = roomNotificationChannelManager.getChannelIdForRoom(
                sessionId = sessionId,
                roomId = roomId,
                roomDisplayName = roomDisplayName,
                isDm = isDm,
                noisy = true,
            )
            Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                .putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        putExtra(Settings.EXTRA_CONVERSATION_ID, createShortcutId(sessionId, roomId))
                    }
                    addNewTaskFlagIfNeeded()
                }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", context.packageName, null))
                .apply { addNewTaskFlagIfNeeded() }
        }
    }

    private fun Intent.addNewTaskFlagIfNeeded() {
        if (context !is Activity) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
