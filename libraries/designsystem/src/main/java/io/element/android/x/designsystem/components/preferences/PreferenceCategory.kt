package io.element.android.x.designsystem.components.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PreferenceCategory(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            style = MaterialTheme.typography.titleSmall,
            text = title
        )
        content()
    }
}

@Composable
@Preview(showBackground = false)
fun PreferenceCategoryPreview() {
    PreferenceCategory(
        title = "Category title",
    ) {
        PreferenceTextPreview()
        PreferenceSwitchPreview()
        PreferenceSlidePreview()
    }
}
