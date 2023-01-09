package io.element.android.x.features.rageshake.crash.ui

sealed interface CrashDetectionEvents {
    object ResetAll : CrashDetectionEvents
    object ResetAppHasCrashed : CrashDetectionEvents
}
