/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.components.dialogs.ListOption
import io.element.android.libraries.designsystem.components.dialogs.SingleSelectionDialog
import io.element.android.libraries.designsystem.components.dialogs.listOptionOf
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
fun SingleSelectionListItem(
    headline: String,
    options: ImmutableList<ListOption>,
    onSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    leadingContent: ListItemContent? = null,
    resultFormatter: (Int) -> String? = { options.getOrNull(it)?.title },
    selected: Int? = null,
    displayResultInTrailingContent: Boolean = false,
) {
    val coroutineScope = rememberCoroutineScope()

    var selectedIndex by rememberSaveable(selected) { mutableStateOf(selected) }
    val selectedItem by remember { derivedStateOf { selectedIndex?.let { resultFormatter(it) } } }
    val decoratedSupportedText: @Composable (() -> Unit)? = if (!selectedItem.isNullOrBlank() && !displayResultInTrailingContent) {
        @Composable {
            Text(selectedItem!!)
        }
    } else {
        supportingText?.let {
            @Composable {
                Text(it)
            }
        }
    }
    val trailingContent: ListItemContent? = if (!selectedItem.isNullOrBlank() && displayResultInTrailingContent) {
        ListItemContent.Text(selectedItem!!)
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
            onSelectOption = { index ->
                if (index != selectedIndex) {
                    onSelectionChange(index)
                    selectedIndex = index
                }
                // Delay hiding the dialog for a bit so the new state is displayed in it before being dismissed
                coroutineScope.launch {
                    delay(0.5.seconds)
                    displaySelectionDialog = false
                }
            },
            onDismissRequest = { displaySelectionDialog = false },
            initialSelection = selectedIndex,
        )
    }
}

@Preview("Single selection List item - no selection", group = PreviewGroup.ListItems)
@Composable
internal fun SingleSelectionListItemPreview() {
    ElementThemedPreview {
        SingleSelectionListItem(
            headline = "Headline",
            options = listOptionOf("Option 1", "Option 2", "Option 3"),
            onSelectionChange = {},
        )
    }
}

@Preview("Single selection List item - no selection, supporting text", group = PreviewGroup.ListItems)
@Composable
internal fun SingleSelectionListItemUnselectedWithSupportingTextPreview() {
    ElementThemedPreview {
        SingleSelectionListItem(
            headline = "Headline",
            options = listOptionOf("Option 1", "Option 2", "Option 3"),
            supportingText = "Supporting text",
            onSelectionChange = {},
        )
    }
}

@Preview("Single selection List item - selection in supporting text", group = PreviewGroup.ListItems)
@Composable
internal fun SingleSelectionListItemSelectedInSupportingTextPreview() {
    ElementThemedPreview {
        SingleSelectionListItem(
            headline = "Headline",
            options = listOptionOf("Option 1", "Option 2", "Option 3"),
            supportingText = "Supporting text",
            onSelectionChange = {},
            selected = 1,
        )
    }
}

@Preview("Single selection List item - selection in trailing content", group = PreviewGroup.ListItems)
@Composable
internal fun SingleSelectionListItemSelectedInTrailingContentPreview() {
    ElementThemedPreview {
        SingleSelectionListItem(
            headline = "Headline",
            options = listOptionOf("Option 1", "Option 2", "Option 3"),
            supportingText = "Supporting text",
            onSelectionChange = {},
            selected = 1,
            displayResultInTrailingContent = true,
        )
    }
}

@Preview("Single selection List item - custom formatter", group = PreviewGroup.ListItems)
@Composable
internal fun SingleSelectionListItemCustomFormattertPreview() {
    ElementThemedPreview {
        SingleSelectionListItem(
            headline = "Headline",
            options = listOptionOf("Option 1", "Option 2", "Option 3"),
            supportingText = "Supporting text",
            onSelectionChange = {},
            resultFormatter = { "Selected index: $it" },
            selected = 1,
            displayResultInTrailingContent = true,
        )
    }
}
