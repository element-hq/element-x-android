/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.impl

import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.mediaupload.api.MaxUploadSizeProvider

/**
 * Provides the maximum upload size allowed by the Matrix server.
 */
@ContributesBinding(SessionScope::class)
class DefaultMaxUploadSizeProvider(
    private val matrixClient: MatrixClient,
) : MaxUploadSizeProvider {
    override suspend fun getMaxUploadSize(): Result<Long> {
        return matrixClient.getMaxFileUploadSize()
    }
}
