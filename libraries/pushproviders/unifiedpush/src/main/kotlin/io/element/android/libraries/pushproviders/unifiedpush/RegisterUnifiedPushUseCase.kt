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
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PusherSubscriber
import org.unifiedpush.android.connector.UnifiedPush
import javax.inject.Inject

class RegisterUnifiedPushUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pusherSubscriber: PusherSubscriber,
    private val unifiedPushStore: UnifiedPushStore,
) {
    sealed interface RegisterUnifiedPushResult {
        data object Success : RegisterUnifiedPushResult
        data object NeedToAskUserForDistributor : RegisterUnifiedPushResult
        data object Error : RegisterUnifiedPushResult
    }

    suspend fun execute(matrixClient: MatrixClient, distributor: Distributor, clientSecret: String): RegisterUnifiedPushResult {
        val distributorValue = distributor.value
        if (distributorValue.isNotEmpty()) {
            saveAndRegisterApp(distributorValue, clientSecret)
            val endpoint = unifiedPushStore.getEndpoint(clientSecret) ?: return RegisterUnifiedPushResult.Error
            val gateway = unifiedPushStore.getPushGateway(clientSecret) ?: return RegisterUnifiedPushResult.Error
            pusherSubscriber.registerPusher(matrixClient, endpoint, gateway)
            return RegisterUnifiedPushResult.Success
        }

        // TODO Below should never happen?
        if (UnifiedPush.getDistributor(context).isNotEmpty()) {
            registerApp(clientSecret)
            return RegisterUnifiedPushResult.Success
        }

        val distributors = UnifiedPush.getDistributors(context)

        return if (distributors.size == 1) {
            saveAndRegisterApp(distributors.first(), clientSecret)
            RegisterUnifiedPushResult.Success
        } else {
            RegisterUnifiedPushResult.NeedToAskUserForDistributor
        }
    }

    private fun saveAndRegisterApp(distributor: String, clientSecret: String) {
        UnifiedPush.saveDistributor(context, distributor)
        registerApp(clientSecret)
    }

    private fun registerApp(clientSecret: String) {
        UnifiedPush.registerApp(context = context, instance = clientSecret)
    }
}
