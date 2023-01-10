package io.element.android.x.features.rageshake.bugreport

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.net.toUri
import io.element.android.x.architecture.Async
import io.element.android.x.architecture.Presenter
import io.element.android.x.features.rageshake.crash.CrashDataStore
import io.element.android.x.features.rageshake.logs.VectorFileLogger
import io.element.android.x.features.rageshake.reporter.BugReporter
import io.element.android.x.features.rageshake.reporter.ReportType
import io.element.android.x.features.rageshake.screenshot.ScreenshotHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class BugReportPresenter @Inject constructor(
    private val bugReporter: BugReporter,
    private val crashDataStore: CrashDataStore,
    private val screenshotHolder: ScreenshotHolder,
    private val appCoroutineScope: CoroutineScope,
) : Presenter<BugReportState, BugReportEvents> {

    private class BugReporterUploadListener(
        private val sendingProgress: MutableState<Float>,
        private val sendingAction: MutableState<Async<Unit>>
    ) : BugReporter.IMXBugReportListener {

        override fun onUploadCancelled() {
            sendingProgress.value = 0f
            sendingAction.value = Async.Uninitialized
        }

        override fun onUploadFailed(reason: String?) {
            sendingProgress.value = 0f
            sendingAction.value = Async.Failure(Exception(reason))
        }

        override fun onProgress(progress: Int) {
            sendingProgress.value = progress.toFloat() / 100
            sendingAction.value = Async.Loading()
        }

        override fun onUploadSucceed(reportUrl: String?) {
            sendingProgress.value = 0f
            sendingAction.value = Async.Success(Unit)
        }
    }

    @Composable
    override fun present(events: Flow<BugReportEvents>): BugReportState {
        val screenshotUri = rememberSaveable {
            mutableStateOf(
                screenshotHolder.getFile()?.toUri()?.toString()
            )
        }
        val crashInfo: String by crashDataStore
            .crashInfo()
            .collectAsState(initial = "")

        val sendingProgress = remember {
            mutableStateOf(0f)
        }
        val sendingAction: MutableState<Async<Unit>> = remember {
            mutableStateOf(Async.Uninitialized)
        }
        val formState: MutableState<BugReportFormState> = remember {
            mutableStateOf(BugReportFormState.Default)
        }
        val uploadListener = BugReporterUploadListener(sendingProgress, sendingAction)
        val state by remember {
            derivedStateOf {
                BugReportState(
                    hasCrashLogs = crashInfo.isNotEmpty(),
                    sendingProgress = sendingProgress.value,
                    sending = sendingAction.value,
                    formState = formState.value,
                    screenshotUri = screenshotUri.value
                )
            }
        }
        LaunchedEffect(Unit) {
            events.collect { event ->
                when (event) {
                    BugReportEvents.SendBugReport -> appCoroutineScope.sendBugReport(state, uploadListener)
                    BugReportEvents.ResetAll -> appCoroutineScope.resetAll()
                    is BugReportEvents.SetDescription -> updateFormState(formState) {
                        copy(description = event.description)
                    }
                    is BugReportEvents.SetCanContact -> updateFormState(formState) {
                        copy(canContact = event.canContact)
                    }
                    is BugReportEvents.SetSendCrashLog -> updateFormState(formState) {
                        copy(sendCrashLogs = event.sendCrashlog)
                    }
                    is BugReportEvents.SetSendLog -> updateFormState(formState) {
                        copy(sendLogs = event.sendLog)
                    }
                    is BugReportEvents.SetSendScreenshot -> updateFormState(formState) {
                        copy(sendScreenshot = event.sendScreenshot)
                    }
                }
            }
        }
        return state
    }

    private fun updateFormState(formState: MutableState<BugReportFormState>, operation: BugReportFormState.() -> BugReportFormState) {
        formState.value = operation(formState.value)
    }

    private fun CoroutineScope.sendBugReport(state: BugReportState, listener: BugReporter.IMXBugReportListener) = launch {
        bugReporter.sendBugReport(
            coroutineScope = this,
            reportType = ReportType.BUG_REPORT,
            withDevicesLogs = state.formState.sendLogs,
            withCrashLogs = state.hasCrashLogs && state.formState.sendCrashLogs,
            withKeyRequestHistory = false,
            withScreenshot = state.formState.sendScreenshot,
            theBugDescription = state.formState.description,
            serverVersion = "",
            canContact = state.formState.canContact,
            customFields = emptyMap(),
            listener = listener
        )
    }

    private fun CoroutineScope.resetAll() = launch {
        screenshotHolder.reset()
        crashDataStore.reset()
        VectorFileLogger.getFromTimber().reset()
    }
}
