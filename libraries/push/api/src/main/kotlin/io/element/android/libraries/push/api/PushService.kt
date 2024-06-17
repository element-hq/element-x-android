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

package io.element.android.libraries.push.api

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import kotlinx.coroutines.flow.Flow

interface PushService {
    /**
     * Return the current push provider, or null if none.
     */
    suspend fun getCurrentPushProvider(): PushProvider?

    /**
     * Return the list of push providers, available at compile time, sorted by index.
     */
    fun getAvailablePushProviders(): List<PushProvider>

    /**
     * Will unregister any previous pusher and register a new one with the provided [PushProvider].
     *
     * The method has effect only if the [PushProvider] is different than the current one.
     */
    suspend fun registerWith(
        matrixClient: MatrixClient,
        pushProvider: PushProvider,
        distributor: Distributor,
    ): Result<Unit>

    /**
     * Store the given push provider as the current one, but do not register.
     * To be used when there is no distributor available.
     */
    suspend fun selectPushProvider(
        matrixClient: MatrixClient,
        pushProvider: PushProvider,
    )

    fun ignoreRegistrationError(sessionId: SessionId): Flow<Boolean>
    suspend fun setIgnoreRegistrationError(sessionId: SessionId, ignore: Boolean)

    /**
     * Return false in case of early error.
     */
    suspend fun testPush(): Boolean
}
