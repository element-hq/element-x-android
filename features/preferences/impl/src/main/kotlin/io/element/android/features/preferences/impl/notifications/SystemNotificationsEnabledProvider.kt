/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications

import androidx.core.app.NotificationManagerCompat
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import javax.inject.Inject

interface SystemNotificationsEnabledProvider {
    fun notificationsEnabled(): Boolean
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultSystemNotificationsEnabledProvider @Inject constructor(
    private val notificationManager: NotificationManagerCompat,
) : SystemNotificationsEnabledProvider {
    override fun notificationsEnabled(): Boolean {
        return notificationManager.areNotificationsEnabled()
    }
}
