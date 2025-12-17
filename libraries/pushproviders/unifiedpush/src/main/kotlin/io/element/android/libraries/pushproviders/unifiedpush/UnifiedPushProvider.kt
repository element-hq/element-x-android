/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushproviders.api.Config
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret

@ContributesIntoSet(AppScope::class)
@Inject
class UnifiedPushProvider(
    private val unifiedPushDistributorProvider: UnifiedPushDistributorProvider,
    private val registerUnifiedPushUseCase: RegisterUnifiedPushUseCase,
    private val unRegisterUnifiedPushUseCase: UnregisterUnifiedPushUseCase,
    private val pushClientSecret: PushClientSecret,
    private val unifiedPushStore: UnifiedPushStore,
    private val unifiedPushSessionPushConfigProvider: UnifiedPushSessionPushConfigProvider,
) : PushProvider {
    override val index = UnifiedPushConfig.INDEX
    override val name = UnifiedPushConfig.NAME
    override val supportMultipleDistributors = true

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

    override suspend fun getCurrentDistributorValue(sessionId: SessionId): String? {
        return unifiedPushStore.getDistributorValue(sessionId)
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

    override suspend fun getPushConfig(sessionId: SessionId): Config? {
        return unifiedPushSessionPushConfigProvider.provide(sessionId)
    }

    override fun canRotateToken(): Boolean = false
}
