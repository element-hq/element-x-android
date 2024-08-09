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

package io.element.android.libraries.matrix.impl.encryption

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.IdentityOidcResetHandle
import io.element.android.libraries.matrix.api.encryption.IdentityPasswordResetHandle
import io.element.android.libraries.matrix.api.encryption.IdentityResetHandle
import org.matrix.rustcomponents.sdk.AuthData
import org.matrix.rustcomponents.sdk.AuthDataPasswordDetails
import org.matrix.rustcomponents.sdk.CrossSigningResetAuthType

object RustIdentityResetHandleFactory {
    fun create(identityResetHandle: org.matrix.rustcomponents.sdk.IdentityResetHandle): Result<IdentityResetHandle> {
        return runCatching {
            when (val authType = identityResetHandle.authType()) {
                is CrossSigningResetAuthType.Oidc -> RustOidcIdentityResetHandle(identityResetHandle, authType.info.approvalUrl)
                // User interactive authentication (user + password)
                CrossSigningResetAuthType.Uiaa -> RustPasswordIdentityResetHandle(identityResetHandle)
            }
        }
    }
}

class RustPasswordIdentityResetHandle(
    private val identityResetHandle: org.matrix.rustcomponents.sdk.IdentityResetHandle,
) : IdentityPasswordResetHandle {
    override suspend fun resetPassword(userId: UserId, password: String): Result<Unit> {
        return runCatching { identityResetHandle.reset(AuthData.Password(AuthDataPasswordDetails(userId.value, password))) }
    }

    override suspend fun cancel() {
        identityResetHandle.cancelAndDestroy()
    }
}

class RustOidcIdentityResetHandle(
    private val identityResetHandle: org.matrix.rustcomponents.sdk.IdentityResetHandle,
    override val url: String,
) : IdentityOidcResetHandle {
    override suspend fun resetOidc(): Result<Unit> {
        return runCatching { identityResetHandle.reset(null) }
    }

    override suspend fun cancel() {
        identityResetHandle.cancelAndDestroy()
    }
}

private suspend fun org.matrix.rustcomponents.sdk.IdentityResetHandle.cancelAndDestroy() {
    cancel()
    destroy()
}
