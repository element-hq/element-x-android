/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.api.reporter

import java.io.File

interface BugReporter {
    /**
     * Send a bug report.
     *
     * @param withDevicesLogs true to include the device log
     * @param withCrashLogs true to include the crash logs
     * @param withScreenshot true to include the screenshot
     * @param problemDescription the bug description
     * @param canContact true if the user opt in to be contacted directly
     * @param listener the listener
     */
    suspend fun sendBugReport(
        withDevicesLogs: Boolean,
        withCrashLogs: Boolean,
        withScreenshot: Boolean,
        problemDescription: String,
        canContact: Boolean = false,
        listener: BugReporterListener
    )

    /**
     * Provide the log directory.
     */
    fun logDirectory(): File

    /**
     * Set the current tracing log level.
     */
    fun setCurrentTracingLogLevel(logLevel: String)

    /**
     * Save the logcat.
     */
    fun saveLogCat()
}
