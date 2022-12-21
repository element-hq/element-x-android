@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.x.features.preferences

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.x.designsystem.components.preferences.PreferenceScreen
import io.element.android.x.features.rageshake.preferences.RageshakePreferenceCategory
import io.element.android.x.element.resources.R as ElementR

@Composable
fun PreferencesScreen(
    onBackPressed: () -> Unit = {},
) {
    // TODO Hierarchy!
    // TODO Move logout here
    // Include pref from other modules
    PreferencesContent(onBackPressed = onBackPressed)
}

@Composable
fun PreferencesContent(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
) {
    PreferenceScreen(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = stringResource(id = ElementR.string.settings)
    ) {
        RageshakePreferenceCategory()
    }
}

@Preview
@Composable
fun PreferencesContentPreview() {
    PreferencesContent()
}
