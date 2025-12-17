/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.media

import io.element.android.libraries.matrix.api.media.MediaPreviewConfig
import io.element.android.libraries.matrix.api.media.MediaPreviewService
import io.element.android.libraries.matrix.api.media.MediaPreviewValue
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeMediaPreviewService(
    override val mediaPreviewConfigFlow: StateFlow<MediaPreviewConfig> = MutableStateFlow(MediaPreviewConfig.DEFAULT),
    private val fetchMediaPreviewConfigResult: () -> Result<MediaPreviewConfig?> = { lambdaError() },
    private val setMediaPreviewValueResult: (MediaPreviewValue) -> Result<Unit> = { lambdaError() },
    private val setHideInviteAvatarsResult: (Boolean) -> Result<Unit> = { lambdaError() },
) : MediaPreviewService {
    override suspend fun fetchMediaPreviewConfig(): Result<MediaPreviewConfig?> = simulateLongTask {
        fetchMediaPreviewConfigResult()
    }

    override suspend fun setMediaPreviewValue(mediaPreviewValue: MediaPreviewValue): Result<Unit> = simulateLongTask {
        setMediaPreviewValueResult(mediaPreviewValue)
    }

    override suspend fun setHideInviteAvatars(hide: Boolean): Result<Unit> = simulateLongTask {
        setHideInviteAvatarsResult(hide)
    }
}
