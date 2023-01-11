package io.element.android.x.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import io.element.android.x.architecture.Presenter
import io.element.android.x.features.rageshake.bugreport.BugReportPresenter
import io.element.android.x.features.rageshake.crash.ui.CrashDetectionPresenter
import io.element.android.x.features.rageshake.detection.RageshakeDetectionPresenter
import javax.inject.Inject

class RootPresenter @Inject constructor(
    private val bugReportPresenter: BugReportPresenter,
    private val crashDetectionPresenter: CrashDetectionPresenter,
    private val rageshakeDetectionPresenter: RageshakeDetectionPresenter,
) : Presenter<RootState> {

    @Composable
    override fun present(): RootState {
        val isBugReportVisible = rememberSaveable {
            mutableStateOf(false)
        }
        val isShowkaseButtonVisible = rememberSaveable {
            mutableStateOf(true)
        }
        val rageshakeDetectionState = rageshakeDetectionPresenter.present()
        val crashDetectionState = crashDetectionPresenter.present()
        val bugReportState = bugReportPresenter.present()

        fun handleEvent(event: RootEvents) {
            when (event) {
                RootEvents.HideShowkaseButton -> isShowkaseButtonVisible.value = false
            }
        }

        return RootState(
            isBugReportVisible = isBugReportVisible.value,
            isShowkaseButtonVisible = isShowkaseButtonVisible.value,
            rageshakeDetectionState = rageshakeDetectionState,
            crashDetectionState = crashDetectionState,
            bugReportState = bugReportState,
            eventSink = ::handleEvent
        )
    }
}
