/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import io.element.android.libraries.push.api.push.SyncOnNotifiableEvent
import io.element.android.libraries.push.impl.notifications.NotifiableEventResolver
import io.element.android.libraries.push.impl.notifications.NotificationResolverQueue
import io.element.android.libraries.workmanager.api.WorkManagerScheduler

@ContributesTo(AppScope::class)
interface WorkManagerPushBindings {
    fun notifiableEventResolver(): NotifiableEventResolver
    fun notifiableEventQueue(): NotificationResolverQueue
    fun syncOnNotifiableEvent(): SyncOnNotifiableEvent
    fun workManagerScheduler(): WorkManagerScheduler
}
