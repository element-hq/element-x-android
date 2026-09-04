/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.test

import io.element.android.features.enterprise.api.ClientBuilderEnterpriseHook
import io.element.android.libraries.matrix.api.MatrixClientBuilder
import io.element.android.libraries.matrix.api.core.SessionId

class FakeClientBuilderEnterpriseHook(
    private val tweakClientBuilderResult: (MatrixClientBuilder) -> MatrixClientBuilder = { it },
    private val tweakClientBuilderWithSessionIdResult: (MatrixClientBuilder, SessionId) -> MatrixClientBuilder = { clientBuilder, _ -> clientBuilder },
) : ClientBuilderEnterpriseHook {
    override suspend fun tweakClientBuilder(clientBuilder: MatrixClientBuilder): MatrixClientBuilder {
        return tweakClientBuilderResult(clientBuilder)
    }

    override suspend fun tweakClientBuilder(clientBuilder: MatrixClientBuilder, sessionId: SessionId): MatrixClientBuilder {
        return tweakClientBuilderWithSessionIdResult(clientBuilder, sessionId)
    }
}
