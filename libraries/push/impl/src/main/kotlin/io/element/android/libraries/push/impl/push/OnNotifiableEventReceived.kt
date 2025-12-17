/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.push.impl.notifications.DefaultNotificationDrawerManager
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableRingingCallEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface OnNotifiableEventReceived {
    fun onNotifiableEventsReceived(notifiableEvents: List<NotifiableEvent>)
}

@ContributesBinding(AppScope::class)
class DefaultOnNotifiableEventReceived(
    private val defaultNotificationDrawerManager: DefaultNotificationDrawerManager,
    @AppCoroutineScope
    private val coroutineScope: CoroutineScope,
) : OnNotifiableEventReceived {
    override fun onNotifiableEventsReceived(notifiableEvents: List<NotifiableEvent>) {
        coroutineScope.launch {
            defaultNotificationDrawerManager.onNotifiableEventsReceived(notifiableEvents.filter { it !is NotifiableRingingCallEvent })
        }
    }
}
