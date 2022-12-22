package io.element.android.x.features.rageshake.crash.ui

import com.airbnb.mvrx.MavericksState

data class CrashDetectionViewState(
    val crashDetected: Boolean = false,
) : MavericksState
