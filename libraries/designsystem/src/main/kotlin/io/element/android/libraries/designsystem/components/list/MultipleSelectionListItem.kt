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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.components.dialogs.ListOption
import io.element.android.libraries.designsystem.components.dialogs.MultipleSelectionDialog
import io.element.android.libraries.designsystem.components.dialogs.listOptionOf
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun MultipleSelectionListItem(
    headline: String,
    options: ImmutableList<ListOption>,
    onSelectionChange: (List<Int>) -> Unit,
    resultFormatter: (List<Int>) -> String?,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    leadingContent: ListItemContent? = null,
    selected: ImmutableList<Int> = persistentListOf(),
    displayResultInTrailingContent: Boolean = false,
) {
    val selectedIndexes = remember(selected) { selected.toMutableStateList() }
    val selectedItemsText by remember { derivedStateOf { resultFormatter(selectedIndexes) } }

    val decoratedSupportedText: @Composable (() -> Unit)? = when {
        !selectedItemsText.isNullOrBlank() && !displayResultInTrailingContent -> {
            @Composable {
                Text(selectedItemsText!!)
            }
        }
        supportingText != null -> {
            @Composable {
                Text(supportingText)
            }
        }
        else -> null
    }

    val trailingContent: ListItemContent? = if (!selectedItemsText.isNullOrBlank() && displayResultInTrailingContent) {
        ListItemContent.Text(selectedItemsText!!)
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
        MultipleSelectionDialog(
            title = headline,
            options = options,
            onConfirmClick = { newSelectedIndexes ->
                if (newSelectedIndexes != selectedIndexes.toList()) {
                    onSelectionChange(newSelectedIndexes)
                    selectedIndexes.clear()
                    selectedIndexes.addAll(newSelectedIndexes)
                }
                displaySelectionDialog = false
            },
            onDismissRequest = { displaySelectionDialog = false },
            initialSelection = selectedIndexes.toImmutableList(),
        )
    }
}

@Preview("Multiple selection List item - no selection", group = PreviewGroup.ListItems)
@Composable
internal fun MutipleSelectionListItemPreview() {
    ElementThemedPreview {
        val options = listOptionOf("Option 1", "Option 2", "Option 3")
        MultipleSelectionListItem(
            headline = "Headline",
            options = options,
            onSelectionChange = {},
            supportingText = "Supporting text",
            resultFormatter = { result -> formatResult(result, options) },
        )
    }
}

@Preview("Multiple selection List item - selection in supporting text", group = PreviewGroup.ListItems)
@Composable
internal fun MutipleSelectionListItemSelectedPreview() {
    ElementThemedPreview {
        val options = listOptionOf("Option 1", "Option 2", "Option 3")
        val selected = persistentListOf<Int>(0, 2)
        MultipleSelectionListItem(
            headline = "Headline",
            options = options,
            onSelectionChange = {},
            supportingText = "Supporting text",
            resultFormatter = {
                val selectedValues = formatResult(it, options)
                "Selected: $selectedValues"
            },
            selected = selected,
        )
    }
}

@Preview("Multiple selection List item - selection in trailing content", group = PreviewGroup.ListItems)
@Composable
internal fun MutipleSelectionListItemSelectedTrailingContentPreview() {
    ElementThemedPreview {
        val options = listOptionOf("Option 1", "Option 2", "Option 3")
        val selected = persistentListOf<Int>(0, 2)
        MultipleSelectionListItem(
            headline = "Headline",
            options = options,
            onSelectionChange = {},
            supportingText = "Supporting text",
            resultFormatter = { selected.size.toString() },
            displayResultInTrailingContent = true,
            selected = selected,
        )
    }
}

private fun formatResult(result: List<Int>, options: ImmutableList<ListOption>): String? {
    return options.mapIndexedNotNull { index, value -> value.title.takeIf { result.contains(index) } }.joinToString(", ").takeIf { it.isNotEmpty() }
}
