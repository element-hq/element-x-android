/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.list

import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun SwitchListItem(
    headline: String,
    value: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    leadingContent: ListItemContent? = null,
    enabled: Boolean = true,
    style: ListItemStyle = ListItemStyle.Default,
) {
    ListItem(
        modifier = modifier
            .toggleable(
                value = value,
                role = Role.Checkbox,
                enabled = enabled,
                onValueChange = { onChange(!value) }
            ),
        headlineContent = { Text(headline) },
        supportingContent = supportingText?.let { @Composable { Text(it) } },
        leadingContent = leadingContent,
        trailingContent = ListItemContent.Switch(value, null, enabled),
        style = style,
        enabled = enabled,
    )
}
