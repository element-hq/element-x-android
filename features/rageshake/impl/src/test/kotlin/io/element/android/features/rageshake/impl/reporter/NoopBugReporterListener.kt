/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.reporter

import io.element.android.features.rageshake.api.reporter.BugReporterListener

class NoopBugReporterListener : BugReporterListener {
    override fun onUploadCancelled() = Unit
    override fun onUploadFailed(reason: String?) = Unit
    override fun onProgress(progress: Int) = Unit
    override fun onUploadSucceed() = Unit
}
