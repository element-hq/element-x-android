package io.element.android.x.features.preferences.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.x.architecture.Async
import io.element.android.x.designsystem.components.preferences.PreferenceView
import io.element.android.x.element.resources.R
import io.element.android.x.features.logout.LogoutPreferenceState
import io.element.android.x.features.logout.LogoutPreferenceView
import io.element.android.x.features.preferences.user.UserPreferences
import io.element.android.x.features.rageshake.preferences.RageshakePreferencesState
import io.element.android.x.features.rageshake.preferences.RageshakePreferencesView

@Composable
fun PreferencesRootView(
    state: PreferencesRootState,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    onLogoutClicked: () -> Unit = {},
    onOpenRageShake: () -> Unit = {},
    onRageshakeEnabledChanged: (Boolean) -> Unit = {},
    onRageshakeSensitivityChanged: (Float) -> Unit = {},
) {
    // TODO Hierarchy!
    // Include pref from other modules
    PreferenceView(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = stringResource(id = R.string.settings)
    ) {
        UserPreferences(state.myUser)
        RageshakePreferencesView(
            state = state.rageshakeState,
            onOpenRageshake = onOpenRageShake,
            onSensitivityChanged = onRageshakeSensitivityChanged,
            onIsEnabledChanged = onRageshakeEnabledChanged,
        )
        LogoutPreferenceView(
            state = state.logoutState,
            onLogoutClicked = onLogoutClicked,
        )
    }
}

@Preview
@Composable
fun PreferencesContentPreview() {
    val state = PreferencesRootState(
        logoutState = LogoutPreferenceState(),
        rageshakeState = RageshakePreferencesState(),
        myUser = Async.Uninitialized
    )
    PreferencesRootView(state)
}
