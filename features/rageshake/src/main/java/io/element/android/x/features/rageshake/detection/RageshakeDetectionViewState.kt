package io.element.android.x.features.rageshake.detection

import com.airbnb.mvrx.MavericksState

data class RageshakeDetectionViewState(
    val takeScreenshot: Boolean = false,
    val showDialog: Boolean = false,
    val isEnabled: Boolean = true,
    val isStarted: Boolean = false,
    val isSupported: Boolean = false,
    val sensitivity: Float = 0.5f,
) : MavericksState
