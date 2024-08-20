/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.designsystem.components.dialogs

import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.DialogPreview
import io.element.android.libraries.designsystem.theme.components.SimpleAlertDialogContent
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDialog(
    content: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    submitText: String = AlertDialogDefaults.submitText,
) {
    BasicAlertDialog(modifier = modifier, onDismissRequest = onDismiss) {
        AlertDialogContent(
            title = title,
            content = content,
            submitText = submitText,
            onSubmitClick = onDismiss,
        )
    }
}

@Composable
private fun AlertDialogContent(
    content: String,
    onSubmitClick: () -> Unit,
    title: String? = AlertDialogDefaults.title,
    submitText: String = AlertDialogDefaults.submitText,
) {
    SimpleAlertDialogContent(
        title = title,
        content = content,
        submitText = submitText,
        onSubmitClick = onSubmitClick,
    )
}

object AlertDialogDefaults {
    val title: String? @Composable get() = null
    val submitText: String @Composable get() = stringResource(id = CommonStrings.action_ok)
}

@Preview(group = PreviewGroup.Dialogs)
@Composable
internal fun AlertDialogContentPreview() {
    ElementThemedPreview(showBackground = false) {
        DialogPreview {
            AlertDialogContent(
                content = "Content",
                onSubmitClick = {},
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun AlertDialogPreview() = ElementPreview {
    AlertDialog(
        content = "Content",
        onDismiss = {},
    )
}
