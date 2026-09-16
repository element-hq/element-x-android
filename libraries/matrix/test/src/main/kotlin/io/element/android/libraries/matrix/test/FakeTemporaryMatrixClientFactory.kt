/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test

import io.element.android.libraries.matrix.api.TemporaryMatrixClient
import io.element.android.libraries.matrix.api.TemporaryMatrixClientFactory

class FakeTemporaryMatrixClientFactory(
    private val createResult: (String) -> Result<TemporaryMatrixClient> = { Result.success(FakeTemporaryMatrixClient()) },
) : TemporaryMatrixClientFactory {
    override suspend fun create(serverName: String): Result<TemporaryMatrixClient> = createResult(serverName)
}
