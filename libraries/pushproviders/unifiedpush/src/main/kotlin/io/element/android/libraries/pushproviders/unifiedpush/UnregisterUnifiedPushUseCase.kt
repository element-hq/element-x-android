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
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.pushproviders.api.PusherSubscriber
import org.unifiedpush.android.connector.UnifiedPush
import timber.log.Timber
import javax.inject.Inject

class UnregisterUnifiedPushUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val unifiedPushStore: UnifiedPushStore,
    private val pusherSubscriber: PusherSubscriber,
) {
    suspend fun execute(matrixClient: MatrixClient, clientSecret: String) {
        val endpoint = unifiedPushStore.getEndpoint(clientSecret)
        val gateway = unifiedPushStore.getPushGateway(clientSecret)
        if (endpoint != null && gateway != null) {
            try {
                pusherSubscriber.unregisterPusher(matrixClient, endpoint, gateway)
            } catch (e: Exception) {
                Timber.d(e, "Probably unregistering a non existing pusher")
            }
        }
        unifiedPushStore.storeUpEndpoint(null, clientSecret)
        unifiedPushStore.storePushGateway(null, clientSecret)
        UnifiedPush.unregisterApp(context)
    }
}
