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

package io.element.android.libraries.pushstore.api.clientsecret

import io.element.android.libraries.matrix.api.core.SessionId

interface PushClientSecretStore {
    suspend fun storeSecret(userId: SessionId, clientSecret: String)
    suspend fun getSecret(userId: SessionId): String?
    suspend fun resetSecret(userId: SessionId)
    suspend fun getUserIdFromSecret(clientSecret: String): SessionId?
}
