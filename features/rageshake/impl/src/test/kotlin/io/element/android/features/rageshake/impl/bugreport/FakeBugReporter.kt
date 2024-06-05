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

package io.element.android.features.rageshake.impl.bugreport

import io.element.android.features.rageshake.api.reporter.BugReporter
import io.element.android.features.rageshake.api.reporter.BugReporterListener
import io.element.android.libraries.matrix.test.A_FAILURE_REASON
import kotlinx.coroutines.delay
import java.io.File

class FakeBugReporter(val mode: Mode = Mode.Success) : BugReporter {
    enum class Mode {
        Success,
        Failure,
        Cancel
    }

    override suspend fun sendBugReport(
        withDevicesLogs: Boolean,
        withCrashLogs: Boolean,
        withScreenshot: Boolean,
        problemDescription: String,
        canContact: Boolean,
        listener: BugReporterListener,
    ) {
        delay(100)
        listener.onProgress(0)
        delay(100)
        listener.onProgress(50)
        delay(100)
        when (mode) {
            Mode.Success -> Unit
            Mode.Failure -> {
                listener.onUploadFailed(A_FAILURE_REASON)
                return
            }
            Mode.Cancel -> {
                listener.onUploadCancelled()
                return
            }
        }
        listener.onProgress(100)
        delay(100)
        listener.onUploadSucceed()
    }

    override fun logDirectory(): File {
        return File("fake")
    }

    override fun setCurrentTracingFilter(tracingFilter: String) {
        // No op
    }

    override fun saveLogCat() {
        // No op
    }
}
