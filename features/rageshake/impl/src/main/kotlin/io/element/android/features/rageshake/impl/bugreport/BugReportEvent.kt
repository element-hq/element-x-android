/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.bugreport

sealed interface BugReportEvent {
    data object SendBugReport : BugReportEvent
    data object ResetAll : BugReportEvent
    data object ClearError : BugReportEvent

    data class SetDescription(val description: String) : BugReportEvent
    data class SetSendLog(val sendLog: Boolean) : BugReportEvent
    data class SetCanContact(val canContact: Boolean) : BugReportEvent
    data class SetSendScreenshot(val sendScreenshot: Boolean) : BugReportEvent
    data class SetSendPushRules(val sendPushRules: Boolean) : BugReportEvent
    data class SetGhIssueNumber(val ghIssueNumber: Int?) : BugReportEvent
}
