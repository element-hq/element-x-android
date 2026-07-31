/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test

import io.element.android.libraries.matrix.api.TemporaryMatrixClientFactory
import io.element.android.libraries.matrix.api.UnauthenticatedMatrixClient

class FakeTemporaryMatrixClientFactory(
    private val createResult: (String) -> Result<UnauthenticatedMatrixClient> = { Result.success(FakeUnauthenticatedMatrixClient()) },
) : TemporaryMatrixClientFactory {
    override suspend fun createTemporaryMatrixClient(homeServerUrl: String): Result<UnauthenticatedMatrixClient> = createResult(homeServerUrl)
}
