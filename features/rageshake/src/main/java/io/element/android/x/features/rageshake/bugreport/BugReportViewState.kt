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

package io.element.android.x.features.rageshake.bugreport

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized

data class BugReportViewState(
    val formState: BugReportFormState = BugReportFormState.Default,
    val sendLogs: Boolean = true,
    val hasCrashLogs: Boolean = false,
    val sendCrashLogs: Boolean = true,
    val canContact: Boolean = false,
    val sendScreenshot: Boolean = false,
    val screenshotUri: String? = null,
    val sendingProgress: Float = 0F,
    val sending: Async<Unit> = Uninitialized,
) : MavericksState {
    val submitEnabled =
        formState.description.length > 10 && sending !is Loading
}

data class BugReportFormState(
    val description: String,
) {
    companion object {
        val Default = BugReportFormState("")
    }
}
