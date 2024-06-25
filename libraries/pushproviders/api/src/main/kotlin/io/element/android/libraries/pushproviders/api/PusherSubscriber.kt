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
import io.element.android.libraries.matrix.api.exception.ClientException

interface PusherSubscriber {
    /**
     * Register a pusher. Note that failure will be a [RegistrationFailure].
     */
    suspend fun registerPusher(matrixClient: MatrixClient, pushKey: String, gateway: String): Result<Unit>

    /**
     * Unregister a pusher.
     */
    suspend fun unregisterPusher(matrixClient: MatrixClient, pushKey: String, gateway: String): Result<Unit>
}

class RegistrationFailure(
    val clientException: ClientException,
    val isRegisteringAgain: Boolean
) : Exception(clientException)
