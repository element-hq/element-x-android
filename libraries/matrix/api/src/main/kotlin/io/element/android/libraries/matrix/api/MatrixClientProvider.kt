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

package io.element.android.libraries.matrix.api

import io.element.android.libraries.matrix.api.core.SessionId

interface MatrixClientProvider {
    /**
     * Can be used to get or restore a MatrixClient with the given [SessionId].
     * If a [MatrixClient] is already in memory, it'll return it. Otherwise it'll try to restore one.
     * Most of the time you want to use injected constructor instead of retrieving a MatrixClient with this provider.
     */
    suspend fun getOrRestore(sessionId: SessionId): Result<MatrixClient>

    /**
     * Can be used to retrieve an existing [MatrixClient] with the given [SessionId].
     * @param sessionId the [SessionId] of the [MatrixClient] to retrieve.
     * @return the [MatrixClient] if it exists.
     */
    fun getOrNull(sessionId: SessionId): MatrixClient?
}
