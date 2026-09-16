/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.tracing

import timber.log.Timber

/**
 * Routes the app's own logs into the SDK's tracing subsystem, so that Kotlin and Rust logs end up interleaved in the same files.
 */
interface TracingService {
    /**
     * Creates a Timber tree that forwards what the app logs to the SDK tracing subsystem.
     *
     * @param target the tracing target name the logs are attributed to, used to tell subsystems apart in the output.
     */
    fun createTimberTree(target: String): Timber.Tree

    /**
     * Turns writing logs to files on or off at runtime, which is how the user-facing logging toggle is applied.
     *
     * @param config where to write the log files, or the disabled configuration to stop writing them.
     */
    fun updateWriteToFilesConfiguration(config: WriteToFilesConfiguration)
}
