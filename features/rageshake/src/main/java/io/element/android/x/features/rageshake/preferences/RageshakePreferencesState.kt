package io.element.android.x.features.rageshake.preferences

data class RageshakePreferencesState(
    val isEnabled: Boolean = false,
    val isSupported: Boolean = true,
    val sensitivity: Float = 0.3f,
    val eventSink: (RageshakePreferencesEvents) -> Unit = {},
)
