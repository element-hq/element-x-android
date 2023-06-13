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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup

@Preview(group = PreviewGroup.Menus)
@Composable
internal fun MenuPreview() {
    ElementThemedPreview {
        var isExpanded by remember { mutableStateOf(false) }
        Button(onClick = { isExpanded = !isExpanded }) {
            Text("Toggle")
        }
        DropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
            for (i in 0..5) {
                val leadingIcon: @Composable (() -> Unit)? = if (i in 2..3) {
                    @Composable {
                        Icon(Icons.Filled.Favorite, contentDescription = "Favorite")
                    }
                } else {
                    null
                }

                val trailingIcon: @Composable (() -> Unit)? = if (i in 3..4) {
                    @Composable {
                        Icon(Icons.Filled.ArrowRight, contentDescription = "Favorite")
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
