package io.element.android.x.features.preferences.root

import io.element.android.x.architecture.Async
import io.element.android.x.features.logout.LogoutPreferenceState
import io.element.android.x.features.rageshake.preferences.RageshakePreferencesState
import io.element.android.x.matrix.ui.model.MatrixUser

data class PreferencesRootState(
    val logoutState: LogoutPreferenceState,
    val rageshakeState: RageshakePreferencesState,
    val myUser: Async<MatrixUser>,
)
