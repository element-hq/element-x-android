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
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.unifiedpush.registration.EndpointRegistrationHandler
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.unifiedpush.android.connector.UnifiedPush
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

interface RegisterUnifiedPushUseCase {
    suspend fun execute(distributor: Distributor, clientSecret: String): Result<Unit>
}

@ContributesBinding(AppScope::class)
class DefaultRegisterUnifiedPushUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val endpointRegistrationHandler: EndpointRegistrationHandler,
    private val coroutineScope: CoroutineScope,
) : RegisterUnifiedPushUseCase {
    override suspend fun execute(distributor: Distributor, clientSecret: String): Result<Unit> {
        UnifiedPush.saveDistributor(context, distributor.value)
        val completable = CompletableDeferred<Result<Unit>>()
        val job = coroutineScope.launch {
            val result = endpointRegistrationHandler.state
                .filter { it.clientSecret == clientSecret }
                .first()
                .result
            completable.complete(result)
        }
        // This will trigger the callback
        // VectorUnifiedPushMessagingReceiver.onNewEndpoint
        UnifiedPush.registerApp(context = context, instance = clientSecret)
        // Wait for VectorUnifiedPushMessagingReceiver.onNewEndpoint to proceed
        return withTimeout(30.seconds) {
            completable.await()
        }
            .onFailure {
                job.cancel()
            }
    }
}
