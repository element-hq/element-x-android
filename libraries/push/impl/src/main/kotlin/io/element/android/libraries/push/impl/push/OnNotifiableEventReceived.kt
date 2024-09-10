/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.push.impl.notifications.DefaultNotificationDrawerManager
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

interface OnNotifiableEventReceived {
    fun onNotifiableEventReceived(notifiableEvent: NotifiableEvent)
}

@ContributesBinding(AppScope::class)
class DefaultOnNotifiableEventReceived @Inject constructor(
    private val defaultNotificationDrawerManager: DefaultNotificationDrawerManager,
    private val coroutineScope: CoroutineScope,
    private val syncOnNotifiableEvent: SyncOnNotifiableEvent,
) : OnNotifiableEventReceived {
    override fun onNotifiableEventReceived(notifiableEvent: NotifiableEvent) {
        coroutineScope.launch {
            launch { syncOnNotifiableEvent(notifiableEvent) }
            defaultNotificationDrawerManager.onNotifiableEventReceived(notifiableEvent)
        }
    }
}
