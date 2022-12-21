package io.element.android.x.designsystem.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import io.element.android.x.element.resources.R as ElementR

@Composable
fun ConfirmationDialog(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
    submitText: String = stringResource(id = ElementR.string.ok),
    cancelText: String = stringResource(id = ElementR.string.action_cancel),
    thirdButtonText: String? = null,
    onSubmitClicked: () -> Unit = {},
    onCancelClicked: () -> Unit = {},
    onThirdButtonClicked: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Text(content)
        },
        dismissButton = {
            Row(
                modifier = Modifier.padding(all = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Column {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onCancelClicked()
                        }) {
                        Text(cancelText)
                    }
                    if (thirdButtonText != null) {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                onThirdButtonClicked()
                            }) {
                            Text(thirdButtonText)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.padding(all = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onSubmitClicked()
                    }
                ) {
                    Text(submitText)
                }
            }
        },
    )
}

@Composable
@Preview
fun ConfirmationDialogPreview() {
    ConfirmationDialog(
        title = "Title",
        content = "Content",
        thirdButtonText = "Disable"
    )
}
