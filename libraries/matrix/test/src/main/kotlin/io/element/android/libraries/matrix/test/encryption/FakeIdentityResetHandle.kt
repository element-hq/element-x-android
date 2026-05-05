/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.encryption

import io.element.android.libraries.matrix.api.encryption.IdentityOAuthResetHandle
import io.element.android.libraries.matrix.api.encryption.IdentityPasswordResetHandle

class FakeIdentityOAuthResetHandle(
    override val url: String = "",
    var resetOAuthLambda: () -> Result<Unit> = { error("Not implemented") },
    var cancelLambda: () -> Unit = { error("Not implemented") },
) : IdentityOAuthResetHandle {
    override suspend fun resetOAuth(): Result<Unit> {
        return resetOAuthLambda()
    }

    override suspend fun cancel() {
        cancelLambda()
    }
}

class FakeIdentityPasswordResetHandle(
    var resetPasswordLambda: (String) -> Result<Unit> = { _ -> error("Not implemented") },
    var cancelLambda: () -> Unit = { error("Not implemented") },
) : IdentityPasswordResetHandle {
    override suspend fun resetPassword(password: String): Result<Unit> {
        return resetPasswordLambda(password)
    }

    override suspend fun cancel() {
        cancelLambda()
    }
}
