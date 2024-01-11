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
import com.squareup.anvil.annotations.ContributesMultibinding
import io.element.android.libraries.androidutils.system.getApplicationLabel
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import org.unifiedpush.android.connector.UnifiedPush
import javax.inject.Inject

@ContributesMultibinding(AppScope::class)
class UnifiedPushProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val registerUnifiedPushUseCase: RegisterUnifiedPushUseCase,
    private val unRegisterUnifiedPushUseCase: UnregisterUnifiedPushUseCase,
    private val pushClientSecret: PushClientSecret,
) : PushProvider {
    override val index = UnifiedPushConfig.INDEX
    override val name = UnifiedPushConfig.NAME

    override fun getDistributors(): List<Distributor> {
        val distributors = UnifiedPush.getDistributors(context)
        return distributors.mapNotNull {
            if (it == context.packageName) {
                // Exclude self
                null
            } else {
                Distributor(it, context.getApplicationLabel(it))
            }
        }
    }

    override suspend fun registerWith(matrixClient: MatrixClient, distributor: Distributor) {
        val clientSecret = pushClientSecret.getSecretForUser(matrixClient.sessionId)
        registerUnifiedPushUseCase.execute(matrixClient, distributor, clientSecret)
    }

    override suspend fun unregister(matrixClient: MatrixClient) {
        val clientSecret = pushClientSecret.getSecretForUser(matrixClient.sessionId)
        unRegisterUnifiedPushUseCase.execute(clientSecret)
    }

    override suspend fun troubleshoot(): Result<Unit> {
        TODO("Not yet implemented")
    }
}
