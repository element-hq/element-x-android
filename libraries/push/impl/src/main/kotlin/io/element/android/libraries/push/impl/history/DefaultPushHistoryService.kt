/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.history

import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.core.content.getSystemService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.impl.PushDatabase
import io.element.android.libraries.push.impl.db.PushHistory
import io.element.android.services.toolbox.api.systemclock.SystemClock

@ContributesBinding(AppScope::class)
class DefaultPushHistoryService(
    private val pushDatabase: PushDatabase,
    private val systemClock: SystemClock,
    @ApplicationContext context: Context,
) : PushHistoryService {
    private val powerManager = context.getSystemService<PowerManager>()
    private val packageName = context.packageName

    override fun onPushReceived(
        providerInfo: String,
        eventId: EventId?,
        roomId: RoomId?,
        sessionId: SessionId?,
        hasBeenResolved: Boolean,
        includeDeviceState: Boolean,
        comment: String?,
    ) {
        val finalComment = buildString {
            append(comment.orEmpty())
            if (includeDeviceState && powerManager != null) {
                // Add info about device state
                append("\n")
                append(" - Idle: ${powerManager.isDeviceIdleMode}\n")
                append(" - Power Save Mode: ${powerManager.isPowerSaveMode}\n")
                append(" - Ignoring Battery Optimizations: ${powerManager.isIgnoringBatteryOptimizations(packageName)}\n")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    append(" - Device Light Idle Mode: ${powerManager.isDeviceLightIdleMode}\n")
                    append(" - Low Power Standby Enabled: ${powerManager.isLowPowerStandbyEnabled}\n")
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    append(" - Exempt from Low Power Standby: ${powerManager.isExemptFromLowPowerStandby}\n")
                }
            }
        }.takeIf { it.isNotEmpty() }

        pushDatabase.pushHistoryQueries.insertPushHistory(
            PushHistory(
                pushDate = systemClock.epochMillis(),
                providerInfo = providerInfo,
                eventId = eventId?.value,
                roomId = roomId?.value,
                sessionId = sessionId?.value,
                hasBeenResolved = if (hasBeenResolved) 1 else 0,
                comment = finalComment,
            )
        )

        // Keep only the last 1_000 events
        pushDatabase.pushHistoryQueries.removeOldest(1_000)
    }
}
