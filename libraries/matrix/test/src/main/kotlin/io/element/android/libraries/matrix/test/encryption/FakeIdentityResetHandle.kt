/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.matrix.test.encryption

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.IdentityOidcResetHandle
import io.element.android.libraries.matrix.api.encryption.IdentityPasswordResetHandle

class FakeIdentityOidcResetHandle(
    override val url: String = "",
    var resetOidcLambda: () -> Result<Unit> = { error("Not implemented") },
    var cancelLambda: () -> Unit = { error("Not implemented") },
) : IdentityOidcResetHandle {
    override suspend fun resetOidc(): Result<Unit> {
        return resetOidcLambda()
    }

    override suspend fun cancel() {
        cancelLambda()
    }
}

class FakeIdentityPasswordResetHandle(
    var resetPasswordLambda: (UserId, String) -> Result<Unit> = { _, _ -> error("Not implemented") },
    var cancelLambda: () -> Unit = { error("Not implemented") },
) : IdentityPasswordResetHandle {
    override suspend fun resetPassword(userId: UserId, password: String): Result<Unit> {
        return resetPasswordLambda(userId, password)
    }

    override suspend fun cancel() {
        cancelLambda()
    }
}
