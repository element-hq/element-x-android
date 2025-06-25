/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.media

import io.element.android.libraries.matrix.api.media.MediaPreviewConfig
import io.element.android.libraries.matrix.api.media.MediaPreviewService
import io.element.android.libraries.matrix.api.media.MediaPreviewValue
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeMediaPreviewService(
    private val fetchMediaPreviewConfigResult: () -> Result<MediaPreviewConfig?> = { lambdaError() },
    private val mediaPreviewConfigFlow: Flow<MediaPreviewConfig?> =  flowOf(null),
    private val getMediaPreviewValue: ()-> MediaPreviewValue? = { null },
    private val getHideInviteAvatars: () -> Boolean = { false },
    private val setMediaPreviewValueResult: (MediaPreviewValue) -> Result<Unit> = { lambdaError() },
    private val setHideInviteAvatarsResult: (Boolean) -> Result<Unit> = { lambdaError() },
): MediaPreviewService {

    override suspend fun fetchMediaPreviewConfig(): Result<MediaPreviewConfig?> = simulateLongTask {
        fetchMediaPreviewConfigResult()
    }

    override fun getMediaPreviewConfigFlow(): Flow<MediaPreviewConfig?> {
        return mediaPreviewConfigFlow
    }

    override suspend fun getMediaPreviewValue(): MediaPreviewValue? = simulateLongTask {
        getMediaPreviewValue.invoke()
    }

    override suspend fun getHideInviteAvatars(): Boolean = simulateLongTask {
        getHideInviteAvatars.invoke()
    }

    override suspend fun setMediaPreviewValue(mediaPreviewValue: MediaPreviewValue): Result<Unit> = simulateLongTask {
        setMediaPreviewValueResult(mediaPreviewValue)
    }

    override suspend fun setHideInviteAvatars(hide: Boolean): Result<Unit> = simulateLongTask {
        setHideInviteAvatarsResult(hide)
    }
}
