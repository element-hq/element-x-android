package io.element.android.x.features.rageshake.bugreport

sealed interface BugReportEvents {
    object SendBugReport : BugReportEvents
    object ResetAll: BugReportEvents
    data class SetDescription(val description: String): BugReportEvents
    data class SetSendLog(val sendLog: Boolean): BugReportEvents
    data class SetSendCrashLog(val sendCrashlog: Boolean): BugReportEvents
    data class SetCanContact(val canContact: Boolean): BugReportEvents
    data class SetSendScreenshot(val sendScreenshot: Boolean) : BugReportEvents
}
