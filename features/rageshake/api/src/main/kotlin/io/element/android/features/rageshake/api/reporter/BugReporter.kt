/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
     * Set the current tracing filter.
     */
    fun setCurrentTracingFilter(tracingFilter: String)

    /**
     * Save the logcat.
     */
    fun saveLogCat()
}
