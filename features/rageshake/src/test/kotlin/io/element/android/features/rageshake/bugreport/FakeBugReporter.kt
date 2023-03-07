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

package io.element.android.features.rageshake.bugreport

import io.element.android.features.rageshake.reporter.BugReporter
import io.element.android.features.rageshake.reporter.BugReporterListener
import io.element.android.features.rageshake.reporter.ReportType
import io.element.android.libraries.matrix.test.A_FAILURE_REASON
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FakeBugReporter(val mode: FakeBugReporterMode = FakeBugReporterMode.Success) : BugReporter {
    override fun sendBugReport(
        coroutineScope: CoroutineScope,
        reportType: ReportType,
        withDevicesLogs: Boolean,
        withCrashLogs: Boolean,
        withKeyRequestHistory: Boolean,
        withScreenshot: Boolean,
        theBugDescription: String,
        serverVersion: String,
        canContact: Boolean,
        customFields: Map<String, String>?,
        listener: BugReporterListener?,
    ) {
        coroutineScope.launch {
            delay(100)
            listener?.onProgress(0)
            delay(100)
            listener?.onProgress(50)
            delay(100)
            when (mode) {
                FakeBugReporterMode.Success -> Unit
                FakeBugReporterMode.Failure -> {
                    listener?.onUploadFailed(A_FAILURE_REASON)
                    return@launch
                }
                FakeBugReporterMode.Cancel -> {
                    listener?.onUploadCancelled()
                    return@launch
                }
            }
            listener?.onProgress(100)
            delay(100)
            listener?.onUploadSucceed(null)
        }
    }
}

enum class FakeBugReporterMode {
    Success,
    Failure,
    Cancel
}
