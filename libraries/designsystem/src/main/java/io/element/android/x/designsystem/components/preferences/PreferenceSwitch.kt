package io.element.android.x.designsystem.components.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Announcement
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.x.designsystem.components.preferences.components.PreferenceIcon
import io.element.android.x.designsystem.toEnabledColor

@Composable
fun PreferenceSwitch(
    title: String,
    isChecked: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    onCheckedChange: (Boolean) -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = preferenceMinHeight)
            .clickable { onCheckedChange(!isChecked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PreferenceIcon(
                icon = icon,
                enabled = enabled
            )
            Text(
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = enabled.toEnabledColor(),
                text = title
            )
            Checkbox(
                checked = isChecked,
                enabled = enabled,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
@Preview(showBackground = false)
fun PreferenceSwitchPreview() {
    PreferenceSwitch(
        title = "Switch",
        icon = Icons.Default.Announcement,
        isChecked = true
    )
}
