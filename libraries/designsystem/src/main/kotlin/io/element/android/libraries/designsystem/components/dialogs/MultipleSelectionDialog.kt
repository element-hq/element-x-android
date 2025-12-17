/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.dialogs

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.components.list.CheckboxListItem
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.DialogPreview
import io.element.android.libraries.designsystem.theme.components.ListSupportingText
import io.element.android.libraries.designsystem.theme.components.SimpleAlertDialogContent
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultipleSelectionDialog(
    options: ImmutableList<ListOption>,
    onConfirmClick: (List<Int>) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    confirmButtonTitle: String = stringResource(CommonStrings.action_confirm),
    dismissButtonTitle: String = stringResource(CommonStrings.action_cancel),
    title: String? = null,
    subtitle: String? = null,
    initialSelection: ImmutableList<Int> = persistentListOf(),
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
        MultipleSelectionDialogContent(
            title = title,
            subtitle = decoratedSubtitle,
            options = options,
            confirmButtonTitle = confirmButtonTitle,
            onConfirmClick = onConfirmClick,
            dismissButtonTitle = dismissButtonTitle,
            onDismissRequest = onDismissRequest,
            initialSelected = initialSelection,
        )
    }
}

@Composable
private fun MultipleSelectionDialogContent(
    options: ImmutableList<ListOption>,
    confirmButtonTitle: String,
    onConfirmClick: (List<Int>) -> Unit,
    dismissButtonTitle: String,
    onDismissRequest: () -> Unit,
    title: String? = null,
    initialSelected: ImmutableList<Int> = persistentListOf(),
    subtitle: @Composable (() -> Unit)? = null,
) {
    val selectedOptionIndexes = remember { initialSelected.toMutableStateList() }

    fun isSelected(index: Int) = selectedOptionIndexes.any { it == index }

    SimpleAlertDialogContent(
        title = title,
        subtitle = subtitle,
        submitText = confirmButtonTitle,
        onSubmitClick = {
            onConfirmClick(selectedOptionIndexes.toList())
        },
        cancelText = dismissButtonTitle,
        onCancelClick = onDismissRequest,
        applyPaddingToContents = false,
    ) {
        LazyColumn {
            itemsIndexed(options) { index, option ->
                CheckboxListItem(
                    headline = option.title,
                    checked = isSelected(index),
                    onChange = {
                        if (isSelected(index)) {
                            selectedOptionIndexes.remove(index)
                        } else {
                            selectedOptionIndexes.add(index)
                        }
                    },
                    supportingText = option.subtitle,
                    compactLayout = true,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Preview(group = PreviewGroup.Dialogs)
@Composable
internal fun MultipleSelectionDialogContentPreview() {
    ElementThemedPreview(showBackground = false) {
        DialogPreview {
            val options = persistentListOf(
                ListOption("Option 1", "Supporting line text lorem ipsum dolor sit amet, consectetur."),
                ListOption("Option 2"),
                ListOption("Option 3"),
            )
            MultipleSelectionDialogContent(
                title = "Dialog title",
                options = options,
                onConfirmClick = {},
                onDismissRequest = {},
                confirmButtonTitle = "Save",
                dismissButtonTitle = "Cancel",
                initialSelected = persistentListOf(0),
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun MultipleSelectionDialogPreview() = ElementPreview {
    val options = persistentListOf(
        ListOption("Option 1", "Supporting line text lorem ipsum dolor sit amet, consectetur."),
        ListOption("Option 2"),
        ListOption("Option 3"),
    )
    MultipleSelectionDialog(
        title = "Dialog title",
        options = options,
        onConfirmClick = {},
        onDismissRequest = {},
        confirmButtonTitle = "Save",
        dismissButtonTitle = "Cancel",
        initialSelection = persistentListOf(0),
    )
}
