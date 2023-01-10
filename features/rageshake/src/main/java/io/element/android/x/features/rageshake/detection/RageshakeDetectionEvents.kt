package io.element.android.x.features.rageshake.detection

import io.element.android.x.core.screenshot.ImageResult

sealed interface RageshakeDetectionEvents {
    object Dismiss: RageshakeDetectionEvents
    object Disable : RageshakeDetectionEvents
    object StartDetection : RageshakeDetectionEvents
    object StopDetection : RageshakeDetectionEvents
    data class ProcessScreenshot(val imageResult: ImageResult) : RageshakeDetectionEvents
}
