/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.push.impl.notifications.DefaultNotificationDrawerManager
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableRingingCallEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

interface OnNotifiableEventReceived {
    fun onNotifiableEventsReceived(notifiableEvents: List<NotifiableEvent>)
}

@ContributesBinding(AppScope::class)
class DefaultOnNotifiableEventReceived @Inject constructor(
    private val defaultNotificationDrawerManager: DefaultNotificationDrawerManager,
    @AppCoroutineScope
    private val coroutineScope: CoroutineScope,
    private val syncOnNotifiableEvent: SyncOnNotifiableEvent,
) : OnNotifiableEventReceived {
    override fun onNotifiableEventsReceived(notifiableEvents: List<NotifiableEvent>) {
        coroutineScope.launch {
            launch { syncOnNotifiableEvent(notifiableEvents) }
            defaultNotificationDrawerManager.onNotifiableEventsReceived(notifiableEvents.filter { it !is NotifiableRingingCallEvent })
        }
    }
}
