/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.analytics

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.analytics.GetDatabaseSizesUseCase
import io.element.android.libraries.matrix.api.analytics.SdkStoreSizes
import io.element.android.libraries.matrix.api.core.SessionId

@ContributesBinding(AppScope::class)
class DefaultGetDatabaseSizesUseCase(
    private val clientProvider: Lazy<MatrixClientProvider>,
) : GetDatabaseSizesUseCase {
    override suspend fun invoke(sessionId: SessionId): Result<SdkStoreSizes> {
        val client = clientProvider.value.getOrNull(sessionId)
            ?: return Result.failure(IllegalArgumentException("No MatrixClient for session $sessionId"))

        return client.getDatabaseSizes()
    }
}
