/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications

import androidx.core.app.NotificationManagerCompat
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn

interface SystemNotificationsEnabledProvider {
    fun notificationsEnabled(): Boolean
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultSystemNotificationsEnabledProvider(
    private val notificationManager: NotificationManagerCompat,
) : SystemNotificationsEnabledProvider {
    override fun notificationsEnabled(): Boolean {
        return notificationManager.areNotificationsEnabled()
    }
}
