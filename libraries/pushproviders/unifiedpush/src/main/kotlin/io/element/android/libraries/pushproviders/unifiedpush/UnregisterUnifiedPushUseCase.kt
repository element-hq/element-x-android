/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.pushproviders.unifiedpush

import android.content.Context
import io.element.android.libraries.di.ApplicationContext
import org.unifiedpush.android.connector.UnifiedPush
import timber.log.Timber
import javax.inject.Inject

class UnregisterUnifiedPushUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    // private val pushDataStore: PushDataStore,
    private val unifiedPushStore: UnifiedPushStore,
    // private val unifiedPushGatewayResolver: UnifiedPushGatewayResolver,
) {
    suspend fun execute(clientSecret: String) {
        // val mode = BackgroundSyncMode.FDROID_BACKGROUND_SYNC_MODE_FOR_REALTIME
        // pushDataStore.setFdroidSyncBackgroundMode(mode)
        try {
            unifiedPushStore.getEndpoint(clientSecret)?.let {
                Timber.d("Removing $it")
                // TODO pushersManager?.unregisterPusher(it)
            }
        } catch (e: Exception) {
            Timber.d(e, "Probably unregistering a non existing pusher")
        }
        unifiedPushStore.storeUpEndpoint(null, clientSecret)
        unifiedPushStore.storePushGateway(null, clientSecret)
        UnifiedPush.unregisterApp(context)
    }
}
