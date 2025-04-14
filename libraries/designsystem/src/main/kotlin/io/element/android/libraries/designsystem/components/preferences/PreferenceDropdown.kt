/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.libraries.designsystem.components.preferences

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.preferences.components.preferenceIcon
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.theme.components.DropdownMenuItem
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.toEnabledColor
import io.element.android.libraries.designsystem.toSecondaryEnabledColor
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun <T : DropdownOption> PreferenceDropdown(
    title: String,
    selectedOption: T?,
    options: ImmutableList<T>,
    onSelectOption: (T) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    @DrawableRes iconResourceId: Int? = null,
    showIconAreaIfNoIcon: Boolean = false,
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    ListItem(
        modifier = modifier,
        leadingContent = preferenceIcon(
            icon = icon,
            iconResourceId = iconResourceId,
            enabled = enabled,
            showIconAreaIfNoIcon = showIconAreaIfNoIcon,
        ),
        headlineContent = {
            Text(
                style = ElementTheme.typography.fontBodyLgRegular,
                modifier = Modifier.fillMaxWidth(),
                text = title,
                color = enabled.toEnabledColor(),
            )
        },
        supportingContent = supportingText?.let {
            {
                Text(
                    style = ElementTheme.typography.fontBodyMdRegular,
                    text = it,
                    color = enabled.toSecondaryEnabledColor(),
                )
            }
        },
        trailingContent = ListItemContent.Custom(
            content = {
                DropdownTrailingContent(
                    selectedOption = selectedOption,
                    options = options,
                    onSelectOption = onSelectOption,
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = it },
                )
            }
        ),
        onClick = { isDropdownExpanded = true }.takeIf { !isDropdownExpanded },
    )
}

/**
 * A dropdown option that can be used in a [PreferenceDropdown].
 */
interface DropdownOption {
    /**
     * Returns the text to be displayed for this option.
     */
    @Composable
    fun getText(): String
}

@Composable
private fun <T : DropdownOption> DropdownTrailingContent(
    selectedOption: T?,
    options: ImmutableList<T>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelectOption: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = selectedOption?.getText().orEmpty(),
                maxLines = 1,
                style = ElementTheme.typography.fontBodyMdRegular,
                color = ElementTheme.colors.textSecondary,
            )
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            matchTextFieldWidth = false,
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.getText(),
                            style = ElementTheme.typography.fontBodyMdRegular
                        )
                    },
                    onClick = {
                        onSelectOption(option)
                        onExpandedChange(false)
                    },
                )
            }
        }
    }
}

@Preview(group = PreviewGroup.Preferences)
@Composable
internal fun PreferenceDropdownPreview() = ElementThemedPreview {
    val options = listOf(
        object : DropdownOption {
            @Composable
            override fun getText(): String = "Option 1"
        },
        object : DropdownOption {
            @Composable
            override fun getText(): String = "Option 2"
        },
        object : DropdownOption {
            @Composable
            override fun getText(): String = "Option 3"
        },
    ).toImmutableList()

    Column {
        PreferenceDropdown(
            title = "Dropdown",
            supportingText = "Options for dropdown",
            icon = CompoundIcons.Threads(),
            selectedOption = null,
            options = options,
            onSelectOption = {},
        )
        PreferenceDropdown(
            title = "Dropdown",
            supportingText = "Options for dropdown",
            icon = CompoundIcons.Threads(),
            selectedOption = options.first(),
            options = options,
            onSelectOption = {},
        )
    }
}
