/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushstore.test.userpushstore.clientsecret

import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import io.element.android.tests.testutils.lambda.lambdaError

class FakePushClientSecret(
    private val getSecretForUserResult: (SessionId) -> String = { lambdaError() },
    private val getUserIdFromSecretResult: (String) -> SessionId? = { lambdaError() }
) : PushClientSecret {
    override suspend fun getSecretForUser(userId: SessionId): String {
        return getSecretForUserResult(userId)
    }

    override suspend fun getUserIdFromSecret(clientSecret: String): SessionId? {
        return getUserIdFromSecretResult(clientSecret)
    }
}
