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
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import io.element.android.libraries.designsystem.components.list.RadioButtonListItem
import io.element.android.libraries.designsystem.preview.ElementPreview
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
fun SingleSelectionDialog(
    options: ImmutableList<ListOption>,
    onOptionSelected: (Int) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    dismissButtonTitle: String = stringResource(CommonStrings.action_cancel),
    initialSelection: Int? = null,
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
        SingleSelectionDialogContent(
            title = title,
            subtitle = decoratedSubtitle,
            options = options,
            onOptionSelected = onOptionSelected,
            dismissButtonTitle = dismissButtonTitle,
            onDismissRequest = onDismissRequest,
            initialSelection = initialSelection,
        )
    }
}

@Composable
private fun SingleSelectionDialogContent(
    options: ImmutableList<ListOption>,
    onOptionSelected: (Int) -> Unit,
    dismissButtonTitle: String,
    onDismissRequest: () -> Unit,
    title: String? = null,
    initialSelection: Int? = null,
    subtitle: @Composable (() -> Unit)? = null,
) {
    SimpleAlertDialogContent(
        title = title,
        subtitle = subtitle,
        submitText = dismissButtonTitle,
        onSubmitClicked = onDismissRequest,
        applyPaddingToContents = false,
    ) {
        LazyColumn {
            itemsIndexed(options) { index, option ->
                RadioButtonListItem(
                    headline = option.title,
                    supportingText = option.subtitle,
                    selected = index == initialSelection,
                    onSelected = { onOptionSelected(index) },
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
internal fun SingleSelectionDialogContentPreview() {
    ElementPreview(showBackground = false) {
        DialogPreview {
            val options = persistentListOf(
                ListOption("Option 1"),
                ListOption("Option 2"),
                ListOption("Option 3"),
            )
            SingleSelectionDialogContent(
                title = "Dialog title",
                options = options,
                onOptionSelected = {},
                onDismissRequest = {},
                dismissButtonTitle = "Cancel",
                initialSelection = 0
            )
        }
    }
}
