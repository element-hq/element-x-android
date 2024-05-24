/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.pushproviders.test

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.pushproviders.api.CurrentUserPushConfig
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.tests.testutils.lambda.lambdaError

class FakePushProvider(
    override val index: Int = 0,
    override val name: String = "aFakePushProvider",
    private val isAvailable: Boolean = true,
    private val distributors: List<Distributor> = listOf(Distributor("aDistributorValue", "aDistributorName")),
    private val currentUserPushConfig: CurrentUserPushConfig? = null,
    private val registerWithResult: (MatrixClient, Distributor) -> Result<Unit> = { _, _ -> lambdaError() },
    private val unregisterWithResult: (MatrixClient) -> Result<Unit> = { lambdaError() },
) : PushProvider {
    override fun isAvailable(): Boolean = isAvailable

    override fun getDistributors(): List<Distributor> = distributors

    override suspend fun registerWith(matrixClient: MatrixClient, distributor: Distributor): Result<Unit> {
        return registerWithResult(matrixClient, distributor)
    }

    override suspend fun getCurrentDistributor(matrixClient: MatrixClient): Distributor? {
        return distributors.firstOrNull()
    }

    override suspend fun unregister(matrixClient: MatrixClient): Result<Unit> {
        return unregisterWithResult(matrixClient)
    }

    override suspend fun getCurrentUserPushConfig(): CurrentUserPushConfig? {
        return currentUserPushConfig
    }
}
