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

sealed interface BugReportEvents {
    data object SendBugReport : BugReportEvents
    data object ResetAll : BugReportEvents
    data object ClearError : BugReportEvents

    data class SetDescription(val description: String) : BugReportEvents
    data class SetSendLog(val sendLog: Boolean) : BugReportEvents
    data class SetCanContact(val canContact: Boolean) : BugReportEvents
    data class SetSendScreenshot(val sendScreenshot: Boolean) : BugReportEvents
}
