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

package com.element.android.libraries.pushstore.test.userpushstore.clientsecret

import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret

class FakePushClientSecret(
    private val getSecretForUserResult: (SessionId) -> String = { TODO() },
    private val getUserIdFromSecretResult: (String) -> SessionId? = { TODO() }
) : PushClientSecret {
    override suspend fun getSecretForUser(userId: SessionId): String {
        return getSecretForUserResult(userId)
    }

    override suspend fun getUserIdFromSecret(clientSecret: String): SessionId? {
        return getUserIdFromSecretResult(clientSecret)
    }
}
