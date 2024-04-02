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
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider

interface PushService {
    // TODO Move away
    fun notificationStyleChanged()

    /**
     * Return the list of push providers, available at compile time, and
     * available at runtime, sorted by index.
     */
    fun getAvailablePushProviders(): List<PushProvider>

    /**
     * Will unregister any previous pusher and register a new one with the provided [PushProvider].
     *
     * The method has effect only if the [PushProvider] is different than the current one.
     */
    suspend fun registerWith(matrixClient: MatrixClient, pushProvider: PushProvider, distributor: Distributor)

    /**
     * Return false in case of early error.
     */
    suspend fun testPush(): Boolean
}
