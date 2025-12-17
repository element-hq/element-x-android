/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components.previews

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.DropdownMenu
import io.element.android.libraries.designsystem.theme.components.DropdownMenuItem
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text

@Preview(group = PreviewGroup.Menus)
@Composable
internal fun MenuPreview() {
    ElementThemedPreview {
        var isExpanded by remember { mutableStateOf(false) }
        Button(text = "Toggle", onClick = { isExpanded = !isExpanded })
        DropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
            for (i in 0..5) {
                val leadingIcon: @Composable (() -> Unit)? = if (i in 2..3) {
                    @Composable {
                        Icon(
                            imageVector = CompoundIcons.Favourite(),
                            contentDescription = null
                        )
                    }
                } else {
                    null
                }

                val trailingIcon: @Composable (() -> Unit)? = if (i in 3..4) {
                    @Composable {
                        Icon(
                            imageVector = CompoundIcons.ChevronRight(),
                            contentDescription = null,
                        )
                    }
                } else {
                    null
                }
                DropdownMenuItem(
                    text = { Text(text = "Item $i") },
                    onClick = { isExpanded = false },
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                )
            }
        }
    }
}
