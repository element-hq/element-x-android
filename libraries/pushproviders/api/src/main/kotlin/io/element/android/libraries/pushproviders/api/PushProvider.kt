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

package io.element.android.libraries.pushproviders.api

import io.element.android.libraries.matrix.api.MatrixClient

/**
 * This is the main API for this module.
 */
interface PushProvider {
    /**
     * Allow to sort providers, from lower index to higher index.
     */
    val index: Int

    /**
     * User friendly name.
     */
    val name: String

    /**
     * Return true if the push provider is available on this device.
     */
    fun isAvailable(): Boolean

    fun getDistributors(): List<Distributor>

    /**
     * Register the pusher to the homeserver.
     */
    suspend fun registerWith(matrixClient: MatrixClient, distributor: Distributor): Result<Unit>

    /**
     * Return the current distributor, or null if none.
     */
    suspend fun getCurrentDistributor(matrixClient: MatrixClient): Distributor?

    /**
     * Unregister the pusher.
     */
    suspend fun unregister(matrixClient: MatrixClient): Result<Unit>

    suspend fun getCurrentUserPushConfig(): CurrentUserPushConfig?
}
