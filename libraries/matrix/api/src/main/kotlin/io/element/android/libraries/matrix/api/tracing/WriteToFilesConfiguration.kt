/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.tracing

sealed interface WriteToFilesConfiguration {
    data object Disabled : WriteToFilesConfiguration
    data class Enabled(
        val directory: String,
        val filenamePrefix: String,
        val numberOfFiles: Int?,
    ) : WriteToFilesConfiguration {
        // DO NOT CHANGE: suffix *MUST* be "log" for the rageshake server to not rename the file to something generic
        val filenameSuffix = "log"
    }
}
