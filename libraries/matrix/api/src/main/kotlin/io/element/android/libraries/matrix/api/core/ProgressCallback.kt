/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.core

/**
 * Reports the progress of a long running transfer, such as a media upload or download.
 */
interface ProgressCallback {
    /**
     * Called as the transfer advances, possibly many times and from a background thread.
     *
     * @param current the number of bytes transferred so far.
     * @param total the total number of bytes to transfer.
     */
    fun onProgress(current: Long, total: Long)
}
