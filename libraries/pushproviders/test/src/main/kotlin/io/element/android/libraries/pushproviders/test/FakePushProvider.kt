/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.test

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.pushproviders.api.CurrentUserPushConfig
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.tests.testutils.lambda.lambdaError

class FakePushProvider(
    override val index: Int = 0,
    override val name: String = "aFakePushProvider",
    private val distributors: List<Distributor> = listOf(Distributor("aDistributorValue", "aDistributorName")),
    private val currentDistributor: () -> Distributor? = { distributors.firstOrNull() },
    private val currentUserPushConfig: CurrentUserPushConfig? = null,
    private val registerWithResult: (MatrixClient, Distributor) -> Result<Unit> = { _, _ -> lambdaError() },
    private val unregisterWithResult: (MatrixClient) -> Result<Unit> = { lambdaError() },
) : PushProvider {
    override fun getDistributors(): List<Distributor> = distributors

    override suspend fun registerWith(matrixClient: MatrixClient, distributor: Distributor): Result<Unit> {
        return registerWithResult(matrixClient, distributor)
    }

    override suspend fun getCurrentDistributor(matrixClient: MatrixClient): Distributor? {
        return currentDistributor()
    }

    override suspend fun unregister(matrixClient: MatrixClient): Result<Unit> {
        return unregisterWithResult(matrixClient)
    }

    override suspend fun getCurrentUserPushConfig(): CurrentUserPushConfig? {
        return currentUserPushConfig
    }
}
