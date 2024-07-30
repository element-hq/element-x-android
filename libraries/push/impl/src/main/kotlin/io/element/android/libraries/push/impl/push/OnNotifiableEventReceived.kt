/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.push.impl.push

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.MatrixClientProvider
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
    private val matrixClientProvider: MatrixClientProvider,
    private val featureFlagService: FeatureFlagService,
) : OnNotifiableEventReceived {
    override fun onNotifiableEventReceived(notifiableEvent: NotifiableEvent) {
        coroutineScope.launch {
            subscribeToRoomIfNeeded(notifiableEvent)
            defaultNotificationDrawerManager.onNotifiableEventReceived(notifiableEvent)
        }
    }

    private fun CoroutineScope.subscribeToRoomIfNeeded(notifiableEvent: NotifiableEvent) = launch {
        if (!featureFlagService.isFeatureEnabled(FeatureFlags.SyncOnPush)) {
            return@launch
        }
        val client = matrixClientProvider.getOrRestore(notifiableEvent.sessionId).getOrNull() ?: return@launch
        client.getRoom(notifiableEvent.roomId)?.use { room ->
            room.subscribeToSync()
        }
    }
}
