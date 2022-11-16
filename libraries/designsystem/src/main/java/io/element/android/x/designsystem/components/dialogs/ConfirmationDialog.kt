package io.element.android.x.designsystem.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ConfirmationDialog(
    isDisplayed: MutableState<Boolean>,
    title: String,
    content: String,
    submitText: String = "OK",
    cancelText: String = "Cancel",
    onSubmitClicked: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    if (!isDisplayed.value) return
    AlertDialog(
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
                        isDisplayed.value = false
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
                        isDisplayed.value = false
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
private fun ConfirmationDialogPreview() {
    ConfirmationDialog(
        isDisplayed = remember { mutableStateOf(true) },
        title = "Title",
        content = "Content",
    )
}