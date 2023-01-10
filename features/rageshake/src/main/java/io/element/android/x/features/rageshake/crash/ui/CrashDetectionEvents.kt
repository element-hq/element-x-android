package io.element.android.x.features.rageshake.crash.ui

sealed interface CrashDetectionEvents {
    object ResetAllCrashData : CrashDetectionEvents
    object ResetAppHasCrashed : CrashDetectionEvents
}
