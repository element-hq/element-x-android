package io.element.android.x.designsystem.components.preferences.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.element.android.x.designsystem.toEnabledColor

@Composable
fun PreferenceIcon(
    icon: ImageVector?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    if (icon != null) {
        Icon(
            imageVector = icon,
            contentDescription = "",
            tint = enabled.toEnabledColor(),
            modifier = modifier
                .padding(start = 8.dp)
                .width(48.dp),
        )
    } else {
        Spacer(modifier = modifier.width(56.dp))
    }
}
