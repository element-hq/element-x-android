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
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.pushproviders.api.PusherSubscriber
import org.unifiedpush.android.connector.UnifiedPush
import timber.log.Timber
import javax.inject.Inject

interface UnregisterUnifiedPushUseCase {
    suspend fun execute(matrixClient: MatrixClient, clientSecret: String): Result<Unit>
}

@ContributesBinding(AppScope::class)
class DefaultUnregisterUnifiedPushUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val unifiedPushStore: UnifiedPushStore,
    private val pusherSubscriber: PusherSubscriber,
) : UnregisterUnifiedPushUseCase {
    override suspend fun execute(matrixClient: MatrixClient, clientSecret: String): Result<Unit> {
        val endpoint = unifiedPushStore.getEndpoint(clientSecret)
        val gateway = unifiedPushStore.getPushGateway(clientSecret)
        if (endpoint == null || gateway == null) {
            Timber.w("No endpoint or gateway found for client secret")
            // Ensure we don't have any remaining data, but ignore this error
            unifiedPushStore.storeUpEndpoint(clientSecret, null)
            unifiedPushStore.storePushGateway(clientSecret, null)
            return Result.success(Unit)
        }
        return pusherSubscriber.unregisterPusher(matrixClient, endpoint, gateway)
            .onSuccess {
                unifiedPushStore.storeUpEndpoint(clientSecret, null)
                unifiedPushStore.storePushGateway(clientSecret, null)
                UnifiedPush.unregisterApp(context)
            }
    }
}
