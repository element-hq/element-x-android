package io.element.android.x.designsystem.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ConfirmationDialog(
    isDisplayed: Boolean,
    title: String,
    content: String,
    modifier: Modifier = Modifier,
    submitText: String = "OK",
    cancelText: String = "Cancel",
    onSubmitClicked: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    if (!isDisplayed) return
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Text(content)
        },
        confirmButton = {
            Row(
                modifier = Modifier.padding(all = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onDismiss()
                        onSubmitClicked()
                    })
                {
                    Text(submitText)
                }
            }
        },
        dismissButton = {
            Row(
                modifier = Modifier.padding(all = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onDismiss()
                    }) {
                    Text(cancelText)
                }
            }
        }
    )
}

@Composable
@Preview
fun ConfirmationDialogPreview() {
    ConfirmationDialog(
        isDisplayed = true,
        title = "Title",
        content = "Content",
    )
}
