/*
 * Copyright (c) 2022 New Vector Ltd
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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData

open class BugReportStateProvider : PreviewParameterProvider<BugReportState> {
    override val values: Sequence<BugReportState>
        get() = sequenceOf(
            aBugReportState(),
            aBugReportState().copy(
                formState = BugReportFormState.Default.copy(
                    description = "A long enough description",
                    sendScreenshot = true,
                ),
                hasCrashLogs = true,
                screenshotUri = "aUri"
            ),
            aBugReportState().copy(sending = AsyncData.Loading()),
            aBugReportState().copy(sending = AsyncData.Success(Unit)),
        )
}

fun aBugReportState() = BugReportState(
    formState = BugReportFormState.Default,
    hasCrashLogs = false,
    screenshotUri = null,
    sendingProgress = 0F,
    sending = AsyncData.Uninitialized,
    eventSink = {}
)
