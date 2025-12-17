/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.api.logs

import io.element.android.features.rageshake.api.reporter.BugReporter
import io.element.android.libraries.matrix.api.tracing.WriteToFilesConfiguration

fun BugReporter.createWriteToFilesConfiguration(): WriteToFilesConfiguration {
    return WriteToFilesConfiguration.Enabled(
        directory = logDirectory().absolutePath,
        filenamePrefix = "logs",
        // Keep a maximum of 1 week of log files.
        numberOfFiles = 7 * 24,
    )
}
