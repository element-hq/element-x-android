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

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import io.element.android.libraries.designsystem.components.list.RadioButtonListItem
import io.element.android.libraries.designsystem.preview.DayNightPreviews
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.theme.components.DialogPreview
import io.element.android.libraries.designsystem.theme.components.SimpleAlertDialogContent
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleSelectionDialog(
    options: ImmutableList<String>,
    onOptionSelected: (Int) -> Unit,
    dismissButtonTitle: String = stringResource(CommonStrings.action_cancel),
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        SingleSelectionDialogContent(
            title = title,
            options = options,
            onOptionSelected = onOptionSelected,
            dismissButtonTitle = dismissButtonTitle,
            onDismissRequest = onDismissRequest
        )
    }
}

@Composable
internal fun SingleSelectionDialogContent(
    options: ImmutableList<String>,
    onOptionSelected: (Int) -> Unit,
    onDismissRequest: () -> Unit,
    dismissButtonTitle: String,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    SimpleAlertDialogContent(
        title = title,
        modifier = modifier,
        cancelText = dismissButtonTitle,
        onCancelClicked = onDismissRequest,
        useExternalPaddingForContent = false,
    ) {
        LazyColumn {
            itemsIndexed(options) { index, option ->
                RadioButtonListItem(headline = option, value = false, onSelected = { onOptionSelected(index) }, compactLayout = true)
            }
        }
    }
}

@DayNightPreviews
@ShowkaseComposable(group = PreviewGroup.Dialogs)
@Composable
internal fun SingleSelectionDialogContentPreview() {
    ElementPreview(showBackground = false) {
        DialogPreview {
            val options = persistentListOf("Option 1", "Option 2", "Option 3")
            SingleSelectionDialogContent(title = "Dialog title", options = options, onOptionSelected = {}, onDismissRequest = {}, dismissButtonTitle = "Cancel")
        }
    }
}
