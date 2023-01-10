package io.element.android.x.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import io.element.android.x.architecture.Presenter
import io.element.android.x.architecture.SharedFlowHolder
import io.element.android.x.features.rageshake.bugreport.BugReportEvents
import io.element.android.x.features.rageshake.bugreport.BugReportPresenter
import io.element.android.x.features.rageshake.crash.ui.CrashDetectionEvents
import io.element.android.x.features.rageshake.crash.ui.CrashDetectionPresenter
import io.element.android.x.features.rageshake.detection.RageshakeDetectionEvents
import io.element.android.x.features.rageshake.detection.RageshakeDetectionPresenter
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RootPresenter @Inject constructor(
    private val bugReportPresenter: BugReportPresenter,
    private val crashDetectionPresenter: CrashDetectionPresenter,
    private val rageshakeDetectionPresenter: RageshakeDetectionPresenter,
) : Presenter<RootState, RootEvents> {

    private val rageshakeDetectionEventsFlow = SharedFlowHolder<RageshakeDetectionEvents>()
    private val bugReporterEventsFlow = SharedFlowHolder<BugReportEvents>()
    private val crashDetectionEventsFlow = SharedFlowHolder<CrashDetectionEvents>()

    @Composable
    override fun present(events: Flow<RootEvents>): RootState {
        val isBugReportVisible = rememberSaveable {
            mutableStateOf(false)
        }
        val isShowkaseButtonVisible = rememberSaveable {
            mutableStateOf(true)
        }
        val rageshakeDetectionState = rageshakeDetectionPresenter.present(events = rageshakeDetectionEventsFlow.asSharedFlow())
        val crashDetectionState = crashDetectionPresenter.present(events = crashDetectionEventsFlow.asSharedFlow())
        val bugReportState = bugReportPresenter.present(events = bugReporterEventsFlow.asSharedFlow())

        LaunchedEffect(Unit) {
            events.collect { event ->
                when (event) {
                    RootEvents.HideShowkaseButton -> isShowkaseButtonVisible.value = false
                    RootEvents.ResetAllCrashData -> crashDetectionEventsFlow.emit(CrashDetectionEvents.ResetAllCrashData)
                    RootEvents.ResetAppHasCrashed -> crashDetectionEventsFlow.emit(CrashDetectionEvents.ResetAppHasCrashed)
                    RootEvents.DisableRageshake -> rageshakeDetectionEventsFlow.emit(RageshakeDetectionEvents.Disable)
                    RootEvents.DismissRageshake -> rageshakeDetectionEventsFlow.emit(RageshakeDetectionEvents.Dismiss)
                    RootEvents.StartRageshakeDetection -> rageshakeDetectionEventsFlow.emit(RageshakeDetectionEvents.StartDetection)
                    RootEvents.StopRageshakeDetection -> rageshakeDetectionEventsFlow.emit(RageshakeDetectionEvents.StopDetection)
                    is RootEvents.ProcessScreenshot -> rageshakeDetectionEventsFlow.emit(RageshakeDetectionEvents.ProcessScreenshot(event.imageResult))
                }
            }
        }
        return RootState(
            isBugReportVisible = isBugReportVisible.value,
            isShowkaseButtonVisible = isShowkaseButtonVisible.value,
            rageshakeDetectionState = rageshakeDetectionState,
            crashDetectionState = crashDetectionState,
            bugReportState = bugReportState
        )
    }
}
