/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
    fun create(
        userId: UserId,
        identityResetHandle: org.matrix.rustcomponents.sdk.IdentityResetHandle?
    ): Result<IdentityResetHandle?> {
        return runCatching {
            identityResetHandle?.let {
                when (val authType = identityResetHandle.authType()) {
                    is CrossSigningResetAuthType.Oidc -> RustOidcIdentityResetHandle(identityResetHandle, authType.info.approvalUrl)
                    // User interactive authentication (user + password)
                    CrossSigningResetAuthType.Uiaa -> RustPasswordIdentityResetHandle(userId, identityResetHandle)
                }
            }
        }
    }
}

class RustPasswordIdentityResetHandle(
    private val userId: UserId,
    private val identityResetHandle: org.matrix.rustcomponents.sdk.IdentityResetHandle,
) : IdentityPasswordResetHandle {
    override suspend fun resetPassword(password: String): Result<Unit> {
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
