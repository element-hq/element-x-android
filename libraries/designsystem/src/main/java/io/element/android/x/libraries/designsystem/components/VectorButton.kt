package io.element.android.x.libraries.designsystem.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun VectorButton(text: String, enabled: Boolean, onClick: () -> Unit, modifier: Modifier? = null) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier ?: Modifier
    ) {
        Text(text = text)
    }
}
