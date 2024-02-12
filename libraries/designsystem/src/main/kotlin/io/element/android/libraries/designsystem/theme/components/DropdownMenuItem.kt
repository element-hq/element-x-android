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

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup

// Figma designs: https://www.figma.com/file/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?type=design&node-id=1032%3A44063&mode=design&t=rsNegTbEVLYAXL76-1

@Composable
fun DropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    androidx.compose.material3.DropdownMenuItem(
        text = {
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyLarge) {
                text()
            }
        },
        onClick = onClick,
        modifier = modifier,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        enabled = enabled,
        colors = DropDownMenuItemDefaults.colors(),
        contentPadding = DropDownMenuItemDefaults.contentPadding,
        interactionSource = interactionSource
    )
}

internal object DropDownMenuItemDefaults {
    @Composable
    fun colors() = MenuDefaults.itemColors(
        textColor = ElementTheme.colors.textPrimary,
        leadingIconColor = ElementTheme.colors.iconPrimary,
        trailingIconColor = ElementTheme.colors.iconSecondary,
        disabledTextColor = ElementTheme.colors.textDisabled,
        disabledLeadingIconColor = ElementTheme.colors.iconDisabled,
        disabledTrailingIconColor = ElementTheme.colors.iconDisabled,
    )

    val contentPadding = PaddingValues(all = 12.dp)
}

@Preview(group = PreviewGroup.Menus)
@Composable
internal fun DropdownMenuItemPreview() = ElementThemedPreview {
    Column {
        DropdownMenuItem(
            text = { Text(text = "Item") },
            onClick = {},
            trailingIcon = { Icon(imageVector = CompoundIcons.ChevronRight(), contentDescription = null) },
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(text = "Item") },
            onClick = {},
            leadingIcon = { Icon(imageVector = CompoundIcons.ChatProblem(), contentDescription = null) },
        )
        DropdownMenuItem(
            text = { Text(text = "Item") },
            onClick = {},
            leadingIcon = { Icon(imageVector = CompoundIcons.ChatProblem(), contentDescription = null) },
            trailingIcon = { Icon(imageVector = CompoundIcons.ChevronRight(), contentDescription = null) },
        )
        DropdownMenuItem(
            text = { Text(text = "Item") },
            onClick = {},
            enabled = false,
            leadingIcon = { Icon(imageVector = CompoundIcons.ChatProblem(), contentDescription = null) },
            trailingIcon = { Icon(imageVector = CompoundIcons.ChevronRight(), contentDescription = null) },
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(text = "Multiline\nItem") },
            onClick = {},
            trailingIcon = { Icon(imageVector = CompoundIcons.ChevronRight(), contentDescription = null) },
        )
    }
}
