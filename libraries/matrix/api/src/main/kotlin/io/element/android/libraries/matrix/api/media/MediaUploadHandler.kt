/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.media

/**
 * This is an abstraction over the Rust SDK's `SendAttachmentJoinHandle` which allows us to either [await] the upload process or [cancel] it.
 */
interface MediaUploadHandler {
    /** Await the upload process to finish. */
    suspend fun await(): Result<Unit>

    /** Cancel the upload process. */
    fun cancel()
}
