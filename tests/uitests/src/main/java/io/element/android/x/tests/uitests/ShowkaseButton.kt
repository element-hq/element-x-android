package io.element.android.x.tests.uitests

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ShowkaseButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    var isShowkaseButtonVisible by remember { mutableStateOf(BuildConfig.DEBUG) }

    if (isShowkaseButtonVisible) {
        Button(
            modifier = modifier
                .padding(top = 32.dp),
            onClick = onClick
        ) {
            Text(text = "Showkase Browser")
            IconButton(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(16.dp),
                onClick = { isShowkaseButtonVisible = false },
            ) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "")
            }
        }
    }
}

@Preview(group = "Buttons", name = "Showkase button")
@Composable
fun ShowkaseButtonPreview() {
    ShowkaseButton()
}
