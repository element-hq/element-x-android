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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.x.element.resources.R as ElementR

@Composable
fun ErrorDialog(
    content: String,
    modifier: Modifier = Modifier,
    title: String = stringResource(id = ElementR.string.dialog_title_error),
    submitText: String = stringResource(id = ElementR.string.ok),
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
        confirmButton = {
            Row(
                modifier = Modifier.padding(all = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onDismiss()
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
fun ErrorDialogPreview() {
    ErrorDialog(
        content = "Content",
    )
}
