package io.element.android.x.features.preferences

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.x.designsystem.components.preferences.PreferenceScreen
import io.element.android.x.element.resources.R as ElementR
import io.element.android.x.features.logout.LogoutPreference
import io.element.android.x.features.rageshake.preferences.RageshakePreferences

@Composable
fun PreferencesScreen(
    onBackPressed: () -> Unit = {},
    onOpenRageShake: () -> Unit = {},
    onSuccessLogout: () -> Unit = {},
) {
    // TODO Hierarchy!
    // Include pref from other modules
    PreferencesContent(
        onBackPressed = onBackPressed,
        onOpenRageShake = onOpenRageShake,
        onSuccessLogout = onSuccessLogout,
    )
}

@Composable
fun PreferencesContent(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    onOpenRageShake: () -> Unit = {},
    onSuccessLogout: () -> Unit = {},
) {
    PreferenceScreen(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = stringResource(id = ElementR.string.settings)
    ) {
        LogoutPreference(onSuccessLogout = onSuccessLogout)
        RageshakePreferences(onOpenRageShake = onOpenRageShake)
    }
}

@Preview
@Composable
fun PreferencesContentPreview() {
    PreferencesContent()
}
