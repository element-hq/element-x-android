package io.element.android.x.designsystem.components.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.x.designsystem.components.preferences.components.PreferenceIcon

@Composable
fun PreferenceText(
    title: String,
    // TODO subtitle
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = preferenceMinHeight)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            PreferenceIcon(icon = icon)
            Text(
                modifier = Modifier
                    .weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                text = title
            )
        }
    }
}

@Composable
@Preview(showBackground = false)
fun PreferenceTextPreview() {
    PreferenceText(
        title = "Title",
        icon = Icons.Default.BugReport,
    )
}
