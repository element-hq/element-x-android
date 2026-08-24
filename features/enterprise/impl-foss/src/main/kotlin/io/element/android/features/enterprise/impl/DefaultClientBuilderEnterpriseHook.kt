/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.enterprise.api.ClientBuilderEnterpriseHook
import io.element.android.libraries.matrix.api.MatrixClientBuilder
import io.element.android.libraries.matrix.api.core.SessionId

/**
 * Default implementation of [ClientBuilderEnterpriseHook] that does nothing for FOSS.
 */
@ContributesBinding(AppScope::class)
class DefaultClientBuilderEnterpriseHook : ClientBuilderEnterpriseHook {
    override suspend fun invoke(clientBuilder: MatrixClientBuilder, sessionId: SessionId): MatrixClientBuilder {
        // No modification
        return clientBuilder
    }
}
