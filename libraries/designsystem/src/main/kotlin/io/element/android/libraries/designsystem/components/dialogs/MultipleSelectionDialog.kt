/*
 * Copyright (c) 2023 New Vector Ltd
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

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import io.element.android.libraries.designsystem.components.list.CheckboxListItem
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
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
    onConfirmClicked: (List<Int>) -> Unit,
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
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        MultipleSelectionDialogContent(
            title = title,
            subtitle = decoratedSubtitle,
            options = options,
            confirmButtonTitle = confirmButtonTitle,
            onConfirmClicked = onConfirmClicked,
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
    onConfirmClicked: (List<Int>) -> Unit,
    dismissButtonTitle: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    initialSelected: ImmutableList<Int> = persistentListOf(),
    subtitle: @Composable (() -> Unit)? = null,
) {
    val selectedOptionIndexes = remember { initialSelected.toMutableStateList() }

    fun isSelected(index: Int) = selectedOptionIndexes.any { it == index }

    SimpleAlertDialogContent(
        title = title,
        subtitle = subtitle,
        modifier = modifier,
        submitText = confirmButtonTitle,
        onSubmitClicked = {
            onConfirmClicked(selectedOptionIndexes.toList())
        },
        cancelText = dismissButtonTitle,
        onCancelClicked = onDismissRequest,
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

@PreviewsDayNight
@ShowkaseComposable(group = PreviewGroup.Dialogs)
@Composable
internal fun MultipleSelectionDialogContentPreview() {
    ElementPreview(showBackground = false) {
        DialogPreview {
            val options = persistentListOf(
                ListOption("Option 1", "Supporting line text lorem ipsum dolor sit amet, consectetur."),
                ListOption("Option 2"),
                ListOption("Option 3"),
            )
            MultipleSelectionDialogContent(
                title = "Dialog title",
                options = options,
                onConfirmClicked = {},
                onDismissRequest = {},
                confirmButtonTitle = "Save",
                dismissButtonTitle = "Cancel",
                initialSelected = persistentListOf(0),
            )
        }
    }
}
