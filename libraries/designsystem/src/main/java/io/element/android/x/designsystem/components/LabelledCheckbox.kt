package io.element.android.x.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun LabelledCheckbox(
    checked: Boolean,
    text: String,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit = {},
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
        Text(text = text)
    }
}

@Preview
@Composable
fun LabelledCheckboxPreview() {
    LabelledCheckbox(
        checked = true,
        text = "Some text",
    )
}
