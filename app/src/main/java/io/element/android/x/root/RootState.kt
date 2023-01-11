package io.element.android.x.root

import androidx.compose.runtime.Stable
import io.element.android.x.features.rageshake.bugreport.BugReportState
import io.element.android.x.features.rageshake.crash.ui.CrashDetectionState
import io.element.android.x.features.rageshake.detection.RageshakeDetectionState

@Stable
data class RootState(
    val isBugReportVisible: Boolean,
    val isShowkaseButtonVisible: Boolean,
    val rageshakeDetectionState: RageshakeDetectionState,
    val crashDetectionState: CrashDetectionState,
    val bugReportState: BugReportState,
    val eventSink: (RootEvents) -> Unit
)
