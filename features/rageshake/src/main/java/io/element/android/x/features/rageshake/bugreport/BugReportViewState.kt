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
