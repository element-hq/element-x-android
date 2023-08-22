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

package io.element.android.libraries.designsystem.components.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.element.android.libraries.designsystem.components.dialogs.SingleSelectionDialog
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Text
import kotlinx.collections.immutable.ImmutableList

@Composable
fun SingleSelectionListItem(
    headline: String,
    options: ImmutableList<String>,
    onSelectionChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    leadingContent: ListItemContent? = null,
    selected: Int? = null,
    displayAsTrailingContent: Boolean = false,
) {
    var selectedIndex by rememberSaveable(selected) { mutableStateOf(selected) }
    val selectedItem = remember(selectedIndex) { selectedIndex?.let { options.getOrNull(it) } }
    val decoratedSupportedText: @Composable (() -> Unit)? = if (selectedItem != null && !displayAsTrailingContent) {
        @Composable {
            Text(selectedItem)
        }
    } else if (supportingText != null) {
        @Composable {
            Text(supportingText)
        }
    } else {
        null
    }
    val trailingContent: ListItemContent? = if (selectedItem != null && displayAsTrailingContent) {
        ListItemContent.Text(selectedItem)
    } else {
        null
    }

    var displaySelectionDialog by rememberSaveable { mutableStateOf(false) }

    ListItem(
        modifier = modifier,
        headlineContent = { Text(text = headline) },
        supportingContent = decoratedSupportedText,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        onClick = { displaySelectionDialog = true }
    )

    if (displaySelectionDialog) {
        SingleSelectionDialog(
            title = headline,
            options = options,
            onOptionSelected = { index ->
                if (index != selectedIndex) {
                    onSelectionChanged(index)
                    selectedIndex = index
                }
                displaySelectionDialog = false
            },
            onDismissRequest = { displaySelectionDialog = false },
            initialSelection = selectedIndex,
        )
    }
}
