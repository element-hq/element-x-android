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

import io.element.android.features.rageshake.api.reporter.BugReporterListener
import io.element.android.features.rageshake.api.reporter.ReportType
import kotlinx.coroutines.CoroutineScope

interface BugReporter {
    /**
     * Send a bug report.
     *
     * @param coroutineScope The coroutine scope
     * @param reportType The report type (bug, suggestion, feedback)
     * @param withDevicesLogs true to include the device log
     * @param withCrashLogs true to include the crash logs
     * @param withKeyRequestHistory true to include the crash logs
     * @param withScreenshot true to include the screenshot
     * @param theBugDescription the bug description
     * @param serverVersion version of the server
     * @param canContact true if the user opt in to be contacted directly
     * @param customFields fields which will be sent with the report
     * @param listener the listener
     */
    fun sendBugReport(
        coroutineScope: CoroutineScope,
        reportType: ReportType,
        withDevicesLogs: Boolean,
        withCrashLogs: Boolean,
        withKeyRequestHistory: Boolean,
        withScreenshot: Boolean,
        theBugDescription: String,
        serverVersion: String,
        canContact: Boolean = false,
        customFields: Map<String, String>? = null,
        listener: BugReporterListener?
    )
}
