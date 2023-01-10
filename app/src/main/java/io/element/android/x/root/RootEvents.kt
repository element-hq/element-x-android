package io.element.android.x.root

import io.element.android.x.core.screenshot.ImageResult

sealed interface RootEvents {
    data class ProcessScreenshot(val imageResult: ImageResult) : RootEvents
    object HideShowkaseButton: RootEvents
    object ResetAllCrashData : RootEvents
    object ResetAppHasCrashed: RootEvents
    object DisableRageshake: RootEvents
    object DismissRageshake: RootEvents
    object StartRageshakeDetection: RootEvents
    object StopRageshakeDetection: RootEvents
}
