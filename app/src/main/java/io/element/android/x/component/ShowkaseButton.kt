package io.element.android.x.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun ShowkaseButton(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    onClick: () -> Unit,
    onCloseClicked: () -> Unit
) {
    if (isVisible) {
        Button(
            modifier = Modifier
                .padding(top = 32.dp, start = 16.dp),
            onClick = onClick
        ) {
            Text(text = "Showkase Browser")
            IconButton(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(16.dp),
                onClick = onCloseClicked,
            ) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "")
            }
        }
    }
}
