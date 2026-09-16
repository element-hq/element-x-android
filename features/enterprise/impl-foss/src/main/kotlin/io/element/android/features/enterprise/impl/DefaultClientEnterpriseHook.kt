/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.enterprise.api.ClientEnterpriseHook
import io.element.android.libraries.matrix.api.MatrixClient

/**
 * Default implementation of [ClientEnterpriseHook] that does nothing for FOSS.
 */
@ContributesBinding(AppScope::class)
class DefaultClientEnterpriseHook : ClientEnterpriseHook {
    override suspend fun invoke(client: MatrixClient) {
        // No-op
    }
}
