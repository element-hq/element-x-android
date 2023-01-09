package io.element.android.x.features.rageshake.preferences

sealed interface RageshakePreferencesEvents {
    data class SetSensitivity(val sensitivity: Float) : RageshakePreferencesEvents
    data class SetIsEnabled(val isEnabled: Boolean) : RageshakePreferencesEvents
}
