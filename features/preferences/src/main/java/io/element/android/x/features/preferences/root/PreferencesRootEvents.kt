package io.element.android.x.features.preferences.root

sealed interface PreferencesRootEvents {
    object Logout : PreferencesRootEvents
    data class SetRageshakeSensitivity(val sensitivity: Float) : PreferencesRootEvents
    data class SetRageshakeEnabled(val enabled: Boolean) : PreferencesRootEvents
}
