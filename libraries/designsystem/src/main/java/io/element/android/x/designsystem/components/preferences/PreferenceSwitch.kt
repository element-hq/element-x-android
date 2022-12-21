package io.element.android.x.designsystem.components.preferences

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun PreferenceSwitch(
    title: String,
    isChecked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = preferenceMinHeight),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = modifier
                    .weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                text = title
            )
            Checkbox(checked = isChecked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
@Preview(showBackground = false)
fun PreferenceSwitchPreview() {
    PreferenceSwitch(
        title = "Switch",
        isChecked = true
    )
}
