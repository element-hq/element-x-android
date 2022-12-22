package io.element.android.x.features.rageshake.bugreport

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.core.net.toUri
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.x.anvilannotations.ContributesViewModel
import io.element.android.x.core.di.daggerMavericksViewModelFactory
import io.element.android.x.di.AppScope
import io.element.android.x.features.rageshake.crash.CrashDataStore
import io.element.android.x.features.rageshake.logs.VectorFileLogger
import io.element.android.x.features.rageshake.reporter.BugReporter
import io.element.android.x.features.rageshake.reporter.ReportType
import io.element.android.x.features.rageshake.screenshot.ScreenshotHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@ContributesViewModel(AppScope::class)
class BugReportViewModel @AssistedInject constructor(
    @Assisted initialState: BugReportViewState,
    private val bugReporter: BugReporter,
    private val crashDataStore: CrashDataStore,
    private val screenshotHolder: ScreenshotHolder,
    private val appCoroutineScope: CoroutineScope
) :
    MavericksViewModel<BugReportViewState>(initialState) {

    companion object :
        MavericksViewModelFactory<BugReportViewModel, BugReportViewState> by daggerMavericksViewModelFactory()

    var formState = mutableStateOf(BugReportFormState.Default)
        private set

    init {
        snapshotFlow { formState.value }
            .onEach {
                setState { copy(formState = it) }
            }.launchIn(viewModelScope)
        observerCrashDataStore()
        setState {
            copy(
                screenshotUri = screenshotHolder.getFile()?.toUri()?.toString()
            )
        }
    }

    private fun observerCrashDataStore() {
        viewModelScope.launch {
            crashDataStore.crashInfo().collect {
                setState {
                    copy(
                        hasCrashLogs = it.isNotEmpty()
                    )
                }
            }
        }
    }

    private val listener: BugReporter.IMXBugReportListener = object : BugReporter.IMXBugReportListener {
        override fun onUploadCancelled() {
            setState {
                copy(
                    sendingProgress = 0F,
                    sending = Uninitialized
                )
            }
        }

        override fun onUploadFailed(reason: String?) {
            setState {
                copy(
                    sendingProgress = 0F,
                    sending = Fail(Exception(reason))
                )
            }
        }

        override fun onProgress(progress: Int) {
            setState {
                copy(
                    sendingProgress = progress.toFloat() / 100,
                    sending = Loading()
                )
            }
        }

        override fun onUploadSucceed(reportUrl: String?) {
            setState {
                copy(
                    sendingProgress = 1F,
                    sending = Success(Unit)
                )
            }
        }
    }

    override fun onCleared() {
        // Use appCoroutineScope because we don't want this coroutine to be cancelled
        appCoroutineScope.launch(Dispatchers.IO) {
            screenshotHolder.reset()
            crashDataStore.reset()
            VectorFileLogger.getFromTimber().reset()
        }
        super.onCleared()
    }

    fun onSubmit() {
        setState {
            copy(
                sendingProgress = 0F,
                sending = Loading()
            )
        }
        withState { state ->
            bugReporter.sendBugReport(
                coroutineScope = viewModelScope,
                reportType = ReportType.BUG_REPORT,
                withDevicesLogs = state.sendLogs,
                withCrashLogs = state.hasCrashLogs && state.sendCrashLogs,
                withKeyRequestHistory = false,
                withScreenshot = state.sendScreenshot,
                theBugDescription = state.formState.description,
                serverVersion = "",
                canContact = state.canContact,
                customFields = emptyMap(),
                listener = listener
            )
        }
    }

    fun onFailureDialogClosed() {
        setState {
            copy(
                sendingProgress = 0F,
                sending = Uninitialized
            )
        }
    }

    fun onSetDescription(str: String) {
        formState.value = formState.value.copy(description = str)
        setState { copy(sending = Uninitialized) }
    }

    fun onSetSendLog(value: Boolean) = setState { copy(sendLogs = value) }
    fun onSetSendCrashLog(value: Boolean) = setState { copy(sendCrashLogs = value) }
    fun onSetCanContact(value: Boolean) = setState { copy(canContact = value) }
    fun onSetSendScreenshot(value: Boolean) = setState { copy(sendScreenshot = value) }
}
