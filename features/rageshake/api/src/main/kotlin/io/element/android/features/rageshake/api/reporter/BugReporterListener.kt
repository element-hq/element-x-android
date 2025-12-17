/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.api.reporter

/**
 * Bug report upload listener.
 */
interface BugReporterListener {
    /**
     * The bug report has been cancelled.
     */
    fun onUploadCancelled()

    /**
     * The bug report upload failed.
     *
     * @param reason the failure reason
     */
    fun onUploadFailed(reason: String?)

    /**
     * The upload progress (in percent).
     *
     * @param progress the upload progress
     */
    fun onProgress(progress: Int)

    /**
     * The bug report upload succeeded.
     */
    fun onUploadSucceed()
}
