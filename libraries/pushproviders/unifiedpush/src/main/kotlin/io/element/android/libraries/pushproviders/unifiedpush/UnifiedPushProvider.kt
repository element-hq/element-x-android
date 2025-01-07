/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import com.squareup.anvil.annotations.ContributesMultibinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushproviders.api.CurrentUserPushConfig
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import javax.inject.Inject

@ContributesMultibinding(AppScope::class)
class UnifiedPushProvider @Inject constructor(
    private val unifiedPushDistributorProvider: UnifiedPushDistributorProvider,
    private val registerUnifiedPushUseCase: RegisterUnifiedPushUseCase,
    private val unRegisterUnifiedPushUseCase: UnregisterUnifiedPushUseCase,
    private val pushClientSecret: PushClientSecret,
    private val unifiedPushStore: UnifiedPushStore,
    private val unifiedPushCurrentUserPushConfigProvider: UnifiedPushCurrentUserPushConfigProvider,
) : PushProvider {
    override val index = UnifiedPushConfig.INDEX
    override val name = UnifiedPushConfig.NAME

    override fun getDistributors(): List<Distributor> {
        return unifiedPushDistributorProvider.getDistributors()
    }

    override suspend fun registerWith(matrixClient: MatrixClient, distributor: Distributor): Result<Unit> {
        val clientSecret = pushClientSecret.getSecretForUser(matrixClient.sessionId)
        return registerUnifiedPushUseCase.execute(distributor, clientSecret)
            .onSuccess {
                unifiedPushStore.setDistributorValue(matrixClient.sessionId, distributor.value)
            }
    }

    override suspend fun getCurrentDistributor(sessionId: SessionId): Distributor? {
        val distributorValue = unifiedPushStore.getDistributorValue(sessionId)
        return getDistributors().find { it.value == distributorValue }
    }

    override suspend fun unregister(matrixClient: MatrixClient): Result<Unit> {
        val clientSecret = pushClientSecret.getSecretForUser(matrixClient.sessionId)
        return unRegisterUnifiedPushUseCase.unregister(matrixClient, clientSecret)
    }

    override suspend fun onSessionDeleted(sessionId: SessionId) {
        val clientSecret = pushClientSecret.getSecretForUser(sessionId)
        unRegisterUnifiedPushUseCase.cleanup(clientSecret)
    }

    override suspend fun getCurrentUserPushConfig(): CurrentUserPushConfig? {
        return unifiedPushCurrentUserPushConfigProvider.provide()
    }

    override fun canRotateToken(): Boolean = false
}
