/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.dialogs

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.components.list.TextFieldListItem
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.DialogPreview
import io.element.android.libraries.designsystem.theme.components.ListSupportingText
import io.element.android.libraries.designsystem.theme.components.SimpleAlertDialogContent
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDialog(
    onSubmit: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    cancelText: String = stringResource(CommonStrings.action_cancel),
    submitText: String = stringResource(CommonStrings.action_ok),
    enabled: Boolean = true,
    listItems: LazyListScope.() -> Unit,
) {
    val decoratedSubtitle: @Composable (() -> Unit)? = subtitle?.let {
        @Composable {
            ListSupportingText(
                text = it,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
    BasicAlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        ListDialogContent(
            title = title,
            subtitle = decoratedSubtitle,
            cancelText = cancelText,
            submitText = submitText,
            onDismissRequest = onDismissRequest,
            onSubmitClick = onSubmit,
            enabled = enabled,
            listItems = listItems,
        )
    }
}

@Composable
private fun ListDialogContent(
    listItems: LazyListScope.() -> Unit,
    onDismissRequest: () -> Unit,
    onSubmitClick: () -> Unit,
    cancelText: String,
    submitText: String,
    title: String? = null,
    enabled: Boolean = true,
    subtitle: @Composable (() -> Unit)? = null,
) {
    SimpleAlertDialogContent(
        title = title,
        subtitle = subtitle,
        cancelText = cancelText,
        submitText = submitText,
        onCancelClick = onDismissRequest,
        onSubmitClick = onSubmitClick,
        enabled = enabled,
        applyPaddingToContents = false,
    ) {
        LazyColumn(
            modifier = Modifier.padding(start = 8.dp)
        ) { listItems() }
    }
}

@Preview(group = PreviewGroup.Dialogs)
@Composable
internal fun ListDialogContentPreview() {
    ElementThemedPreview(showBackground = false) {
        DialogPreview {
            ListDialogContent(
                listItems = {
                    item {
                        TextFieldListItem(placeholder = "Text input", text = "", onTextChange = {})
                    }
                    item {
                        TextFieldListItem(placeholder = "Another text input", text = "", onTextChange = {})
                    }
                },
                title = "Dialog title",
                onDismissRequest = {},
                onSubmitClick = {},
                cancelText = "Cancel",
                submitText = "Save",
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun ListDialogPreview() = ElementPreview {
    ListDialog(
        listItems = {
            item {
                TextFieldListItem(placeholder = "Text input", text = "", onTextChange = {})
            }
            item {
                TextFieldListItem(placeholder = "Another text input", text = "", onTextChange = {})
            }
        },
        title = "Dialog title",
        onDismissRequest = {},
        onSubmit = {},
        cancelText = "Cancel",
        submitText = "Save",
    )
}
