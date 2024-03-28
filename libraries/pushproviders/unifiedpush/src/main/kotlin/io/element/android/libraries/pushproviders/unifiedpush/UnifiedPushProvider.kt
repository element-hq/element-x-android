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

import com.squareup.anvil.annotations.ContributesMultibinding
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.pushproviders.api.CurrentUserPushConfig
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import io.element.android.services.appnavstate.api.AppNavigationStateService
import io.element.android.services.appnavstate.api.currentSessionId
import timber.log.Timber
import javax.inject.Inject

private val loggerTag = LoggerTag("UnifiedPushProvider", LoggerTag.PushLoggerTag)

@ContributesMultibinding(AppScope::class)
class UnifiedPushProvider @Inject constructor(
    private val unifiedPushDistributorProvider: UnifiedPushDistributorProvider,
    private val registerUnifiedPushUseCase: RegisterUnifiedPushUseCase,
    private val unRegisterUnifiedPushUseCase: UnregisterUnifiedPushUseCase,
    private val pushClientSecret: PushClientSecret,
    private val unifiedPushStore: UnifiedPushStore,
    private val appNavigationStateService: AppNavigationStateService,
) : PushProvider {
    override val index = UnifiedPushConfig.INDEX
    override val name = UnifiedPushConfig.NAME

    override fun isAvailable(): Boolean {
        val isAvailable = getDistributors().isNotEmpty()
        return if (isAvailable) {
            Timber.tag(loggerTag.value).d("UnifiedPush is available")
            true
        } else {
            Timber.tag(loggerTag.value).w("UnifiedPush is not available")
            false
        }
    }

    override fun getDistributors(): List<Distributor> {
        return unifiedPushDistributorProvider.getDistributors()
    }

    override suspend fun registerWith(matrixClient: MatrixClient, distributor: Distributor) {
        val clientSecret = pushClientSecret.getSecretForUser(matrixClient.sessionId)
        registerUnifiedPushUseCase.execute(matrixClient, distributor, clientSecret)
    }

    override suspend fun unregister(matrixClient: MatrixClient) {
        val clientSecret = pushClientSecret.getSecretForUser(matrixClient.sessionId)
        unRegisterUnifiedPushUseCase.execute(clientSecret)
    }

    override suspend fun getCurrentUserPushConfig(): CurrentUserPushConfig? {
        val currentSession = appNavigationStateService.appNavigationState.value.navigationState.currentSessionId() ?: return null
        val clientSecret = pushClientSecret.getSecretForUser(currentSession)
        val url = unifiedPushStore.getPushGateway(clientSecret) ?: return null
        val pushKey = unifiedPushStore.getEndpoint(clientSecret) ?: return null
        return CurrentUserPushConfig(
            url = url,
            pushKey = pushKey,
        )
    }
}
