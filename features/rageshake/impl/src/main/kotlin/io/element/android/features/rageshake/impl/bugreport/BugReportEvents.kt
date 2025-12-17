/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
    data class SetSendPushRules(val sendPushRules: Boolean) : BugReportEvents
}
