/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.api

import dev.zacsweers.metro.Inject
import io.element.android.libraries.matrix.api.MatrixClient

/**
 * Provides the maximum upload size allowed by the Matrix server.
 */
@Inject
class MaxUploadSizeProvider(
    private val matrixClient: MatrixClient,
) {
    suspend fun getMaxUploadSize(): Result<Long> {
        return matrixClient.getMaxFileUploadSize()
    }
}
