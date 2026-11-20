/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.encryption

import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.IdentityOAuthResetHandle
import io.element.android.libraries.matrix.api.encryption.IdentityPasswordResetHandle
import io.element.android.libraries.matrix.api.encryption.IdentityResetHandle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.AuthData
import org.matrix.rustcomponents.sdk.AuthDataPasswordDetails
import org.matrix.rustcomponents.sdk.CrossSigningResetAuthType

object RustIdentityResetHandleFactory {
    fun create(
        userId: UserId,
        identityResetHandle: org.matrix.rustcomponents.sdk.IdentityResetHandle?,
        dispatcher: CoroutineDispatcher,
    ): Result<IdentityResetHandle?> {
        return runCatchingExceptions {
            identityResetHandle?.let {
                when (val authType = identityResetHandle.authType()) {
                    is CrossSigningResetAuthType.OAuth -> RustIdentityOAuthResetHandle(
                        identityResetHandle,
                        authType.info.approvalUrl,
                        authType.info.session,
                        dispatcher
                    )
                    // User interactive authentication (user + password)
                    is CrossSigningResetAuthType.Uiaa -> RustPasswordIdentityResetHandle(userId, identityResetHandle, authType.info.session, dispatcher)
                }
            }
        }
    }
}

class RustPasswordIdentityResetHandle(
    private val userId: UserId,
    private val identityResetHandle: org.matrix.rustcomponents.sdk.IdentityResetHandle,
    override val session: String,
    private val dispatcher: CoroutineDispatcher,
) : IdentityPasswordResetHandle {
    override suspend fun resetPassword(password: String): Result<Unit> = withContext(dispatcher) {
        runCatchingExceptions {
            identityResetHandle.reset(AuthData.Password(AuthDataPasswordDetails(userId.value, password, session)))
        }
    }

    override suspend fun cancel() = withContext(dispatcher) {
        identityResetHandle.cancelAndDestroy()
    }
}

class RustIdentityOAuthResetHandle(
    private val identityResetHandle: org.matrix.rustcomponents.sdk.IdentityResetHandle,
    override val url: String,
    override val session: String,
    private val dispatcher: CoroutineDispatcher,
) : IdentityOAuthResetHandle {
    override suspend fun resetOAuth(): Result<Unit> = withContext(dispatcher) {
        runCatchingExceptions { identityResetHandle.reset(AuthData.OAuth(session)) }
    }

    override suspend fun cancel() = withContext(dispatcher) {
        identityResetHandle.cancelAndDestroy()
    }
}

private suspend fun org.matrix.rustcomponents.sdk.IdentityResetHandle.cancelAndDestroy() {
    cancel()
    destroy()
}
