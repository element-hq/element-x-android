/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.test

import androidx.compose.runtime.Composable
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.impl.local.LocalMediaActions
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask

class FakeLocalMediaActions(
    val configureResult: () -> Unit = { },
    val saveOnDiskResult: (LocalMedia) -> Result<Unit> = { lambdaError() },
    val shareResult: (LocalMedia) -> Result<Unit> = { lambdaError() },
    val openResult: (LocalMedia) -> Result<Unit> = { lambdaError() },
) : LocalMediaActions {
    @Composable
    override fun Configure() {
        configureResult()
    }

    override suspend fun saveOnDisk(localMedia: LocalMedia): Result<Unit> = simulateLongTask {
        saveOnDiskResult(localMedia)
    }

    override suspend fun share(localMedia: LocalMedia): Result<Unit> = simulateLongTask {
        shareResult(localMedia)
    }

    override suspend fun open(localMedia: LocalMedia): Result<Unit> = simulateLongTask {
        openResult(localMedia)
    }
}
